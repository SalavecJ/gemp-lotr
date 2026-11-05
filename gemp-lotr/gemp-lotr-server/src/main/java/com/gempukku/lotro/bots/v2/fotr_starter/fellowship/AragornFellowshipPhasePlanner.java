package com.gempukku.lotro.bots.v2.fotr_starter.fellowship;

import com.gempukku.lotro.bots.v2.decision.option.*;
import com.gempukku.lotro.bots.v2.decision.request.DecisionRequest;
import com.gempukku.lotro.bots.v2.fotr_starter.FotrStarterDeck;
import com.gempukku.lotro.bots.v2.fotr_starter.Plan;
import com.gempukku.lotro.bots.v2.fotr_starter.SimplePlan;
import com.gempukku.lotro.bots.v2.simulation.SimulatedGame;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;
import org.apache.commons.lang3.NotImplementedException;

import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

import static com.gempukku.lotro.bots.v2.fotr_starter.utility.CardPredicates.*;
import static com.gempukku.lotro.bots.v2.fotr_starter.utility.GameQueries.*;
import static com.gempukku.lotro.bots.v2.fotr_starter.utility.OptionFinders.*;

public class AragornFellowshipPhasePlanner {
    private static final String FRODO = "Frodo, Son of Drogo";
    private static final String SAM = "Sam, Son of Hamfast";
    private static final String ARAGORN = "Aragorn, King in Exile";
    private static final String LEGOLAS = "Legolas, Prince of Mirkwood";
    private static final String BOROMIR = "Boromir, Son of Denethor";
    private static final String WESTFARTHING = "Westfarthing";
    private static final String RIVENDELL_TERRACE = "Rivendell Terrace";
    private static final String MORIA_LAKE = "Moria Lake";
    private static final String PILLARS_OF_THE_KINGS = "Pillars of the Kings";
    private static final String ROSIE = "Rosie Cotton, Hobbiton Lass";
    private static final String ARMOR = "Armor";
    private static final String ATHELAS = "Athelas";
    private static final String COAT_OF_MAIL = "Coat of Mail";
    private static final String GREAT_SHIELD = "Great Shield";
    private static final String HOBBIT_SWORD = "Hobbit Sword";
    private static final String NO_STRANGER = "No Stranger to the Shadows";
    private static final String PATHFINDER = "Pathfinder";
    private static final String EXERT_FRODO_CHOICE = "Exert Frodo";


    public static Plan getPlan(DefaultLotroGame game, String player) {
        SimulatedGame simulatedGame = new SimulatedGame(game);
        List<List<Option>> actionsToTake = new ArrayList<>();
        DecisionRequest nextDecision = simulatedGame.getDecisionRequest(player);

        while (nextDecision != null && simulatedGame.getGame().getGameState().getCurrentPhase() == Phase.FELLOWSHIP) {
            if (isMoriaLakeExertTargetsDecision(nextDecision)) {
                List<Option> chosenOptions = chooseMoriaLakeExertOptions(nextDecision, simulatedGame, player);
                actionsToTake.add(chosenOptions);
                simulatedGame.makeDecision(player, nextDecision.combineSelected(chosenOptions));
            } else {
                Option optionToTake = nextDecision.isFellowshipPhaseDecision()
                        ? chooseFellowshipAction(nextDecision, simulatedGame, player)
                        : chooseFollowUpOption(nextDecision, simulatedGame, player);
                actionsToTake.add(List.of(optionToTake));
                simulatedGame.makeDecision(player, optionToTake.getAnswerForGame());
            }
            nextDecision = simulatedGame.getDecisionRequest(player);
        }

        return new SimplePlan(actionsToTake);
    }

    private record FellowshipRule(Option option, BooleanSupplier condition) {
    }

    private static FellowshipRule rule(Option option) {
        return new FellowshipRule(option, () -> true);
    }

    private static FellowshipRule rule(Option option, BooleanSupplier condition) {
        return new FellowshipRule(option, condition);
    }

    private static Option chooseFellowshipAction(DecisionRequest nextDecision, SimulatedGame simulatedGame, String player) {
        // play cards or activate abilities
        List<Option> options = nextDecision.getOptions();

        Option passOption = findOptionOfType(options, PassOption.class);
        if (passOption == null) {
            throw new IllegalStateException("No pass option available in fellowship phase decision");
        }

        // ordered by priority, the first rule with an available option and a satisfied condition wins
        List<FellowshipRule> rules = List.of(
                // if we can discard a companion to heal, we should do that to get rid of card in hand and get value
                rule(findOptionOfType(options, DiscardCardToHealOption.class)),
                // activate Westfarthing's ability if Frodo can be exerted, before playing companions or allies
                rule(findActivate(options, hasFullName(WESTFARTHING)), () -> wantToActivateWestfarthing(simulatedGame, player)),
                // activate Rivendell Terrace before playing companions
                rule(findActivate(options, hasFullName(RIVENDELL_TERRACE))),
                // always play more companions
                rule(findPlay(options, hasCardType(CardType.COMPANION))),
                // play Rosie if Sam is not dead
                rule(findPlay(options, hasFullName(ROSIE)), () -> wantToPlayRosie(simulatedGame, player)),
                // play Hobbit Sword if companion can bear it
                rule(findPlay(options, hasFullName(HOBBIT_SWORD)), () -> wantToPlayHobbitSword(simulatedGame, player)),
                // always play No Stranger to the Shadows
                rule(findPlay(options, hasFullName(NO_STRANGER))),
                // play Pathfinder if mapping helps, and it is not worth to save for regroup
                rule(findPlay(options, hasFullName(PATHFINDER)), () -> wantToPlayPathfinder(simulatedGame, player)),
                // activate Sam's ability if burdens are present and Sam has enough vitality
                rule(findActivate(options, hasFullName(SAM)), () -> wantToActivateSam(simulatedGame, player)),
                // activate Rosie's ability if Sam is wounded on site 3+
                rule(findActivate(options, hasFullName(ROSIE)), () -> wantToActivateRosie(simulatedGame, player)),
                // activate Athelas if we have good target to heal
                rule(findActivate(options, hasFullName(ATHELAS)), () -> wantToActivateAthelas(simulatedGame, player)),
                // play Armor against Uruks
                rule(findPlay(options, hasFullName(ARMOR)), () -> wantToPlayArmor(simulatedGame, player)),
                // activate Pillars if we have wounded Gondor companion (order matters, cards can get discarded in this step)
                rule(findActivate(options, hasFullName(PILLARS_OF_THE_KINGS)), () -> wantToActivatePillars(simulatedGame, player)),
                // always play Athelas
                rule(findPlay(options, hasFullName(ATHELAS))),
                // play Coat of Mail against Moria, or against Uruks if Armor is already in play, or we have both Aragorn and Boromir in play
                rule(findPlay(options, hasFullName(COAT_OF_MAIL)), () -> wantToPlayCoatOfMail(simulatedGame, player)),
                // play Great Shield against Moria
                rule(findPlay(options, hasFullName(GREAT_SHIELD)), () -> wantToPlayGreatShield(simulatedGame, player))
        );



        // conditions are evaluated lazily, only for rules whose option is available
        return rules.stream()
                .filter(rule -> rule.option() != null && rule.condition().getAsBoolean())
                .map(FellowshipRule::option)
                .findFirst()
                .orElse(passOption);
    }

    private static Option chooseFollowUpOption(DecisionRequest decision, SimulatedGame simulatedGame, String player) {
        // choose targets etc. for the fellowship actions and for the move responses
        List<Option> options = decision.getOptions();

        if (decision.isChooseOptionForCardDecision() && sourceHasFullName(decision, MORIA_LAKE)) {
            return chooseMoriaLakeChoice(options);
        } else if (decision.isChooseTargetForCardDecision() && sourceHasFullName(decision, WESTFARTHING)) {
            return chooseWestfarthingTarget(options, simulatedGame, player);
        } else if (decision.isChooseTargetForCardDecision() && sourceHasFullName(decision, RIVENDELL_TERRACE)) {
            return chooseRivendellTerraceTarget(options);
        } else if (decision.isChooseTargetForCardDecision() && sourceHasFullName(decision, PILLARS_OF_THE_KINGS) && textContains(decision, "discard")) {
            return choosePillarsDiscardTarget(options);
        } else if (decision.isChooseTargetForCardDecision() && sourceHasFullName(decision, PILLARS_OF_THE_KINGS) && textContains(decision, "heal")) {
            return choosePillarsHealTarget(options);
        } else if (decision.isChooseTargetForCardDecision() && sourceHasFullName(decision, HOBBIT_SWORD)) {
            return chooseHobbitSwordTarget(options);
        } else if (decision.isChooseTargetForCardDecision() && sourceHasFullName(decision, ARMOR)) {
            return chooseArmorTarget(options, simulatedGame, player);
        } else if (decision.isChooseTargetForCardDecision() && sourceHasFullName(decision, ATHELAS) && textContains(decision, "Attach")) {
            return chooseAthelasAttachTarget(options);
        } else if (decision.isChooseTargetForCardDecision() && sourceHasFullName(decision, ATHELAS) && textContains(decision, "heal")) {
            return chooseAthelasHealTarget(options);
        } else if (decision.isChooseTargetForCardDecision() && sourceHasFullName(decision, COAT_OF_MAIL)) {
            return chooseCoatOfMailTarget(options);
        } else if (decision.isChooseTargetForCardDecision() && sourceHasFullName(decision, GREAT_SHIELD)) {
            return chooseGreatShieldTarget(options, simulatedGame, player);
        } else {
            throw new NotImplementedException("Unsupported decision: " + decision);
        }
    }

    private static boolean isMoriaLakeExertTargetsDecision(DecisionRequest decision) {
        return !decision.isFellowshipPhaseDecision()
                && decision.isChooseTargetForCardDecision()
                && decision.getSource().getBlueprint().getFullName().equals(MORIA_LAKE);
    }

    private static List<Option> chooseMoriaLakeExertOptions(DecisionRequest decision, SimulatedGame simulatedGame, String player) {
        // exerting Frodo was not possible, need to exert two others
        List<PhysicalCard> targetsToExert = chooseTargetsForMoriaLake(getTargets(decision.getOptions()), simulatedGame, player);
        // choosing two
        return decision.getOptions().stream()
                .filter(option -> option instanceof ChooseTargetForCardOption chooseTargetForCardOption && targetsToExert.stream().anyMatch(target -> target.getCardId() == chooseTargetForCardOption.getTarget().getCardId()))
                .toList();
    }

    private static List<PhysicalCard> chooseTargetsForMoriaLake(List<PhysicalCard> possibleTargets, SimulatedGame simulatedGame, String player) {
        // choose two to exert
        List<PhysicalCard> tbr = new ArrayList<>();

        Map<PhysicalCard, Integer> vitalityMap = new HashMap<>();
        for (PhysicalCard target : possibleTargets) {
            int vitality = simulatedGame.getGame().getModifiersQuerying().getVitality(simulatedGame.getGame(), target);
            vitalityMap.put(target, vitality);
        }

        // whom exerting hurts the least
        List<String> exertPriority = List.of(SAM, ARAGORN, LEGOLAS, BOROMIR);

        int vitalitySaveThreshold = getVitalitySaveThreshold(simulatedGame, player);
        List<PhysicalCard> safeToExertTargets = possibleTargets.stream()
                .filter(target -> vitalityMap.get(target) > vitalitySaveThreshold)
                .toList();
        if (safeToExertTargets.size() == 2) {
            return safeToExertTargets;
        } else if (safeToExertTargets.size() == 1) {
            tbr.add(safeToExertTargets.getFirst());
        } else if (safeToExertTargets.size() >= 3) {
            // choose the two with the highest exert priority
            List<PhysicalCard> sortedTargets = safeToExertTargets.stream()
                    .sorted(Comparator.comparingInt(a -> exertPriority.indexOf(a.getBlueprint().getFullName())))
                    .toList();
            tbr.add(sortedTargets.getFirst());
            tbr.add(sortedTargets.get(1));
        }

        while (tbr.size() < 2) {
            // fill with the highest exert priority
            List<PhysicalCard> sortedTargets = possibleTargets.stream()
                    .filter(target -> !tbr.contains(target))
                    .sorted(Comparator.comparingInt(a -> exertPriority.indexOf(a.getBlueprint().getFullName())))
                    .toList();
            tbr.add(sortedTargets.getFirst());
        }

        return tbr;
    }

    private static Option chooseMoriaLakeChoice(List<Option> options) {
        // choose 'Exert Frodo' option for Moria Lake
        return findChoice(options, EXERT_FRODO_CHOICE)
                .orElseThrow(() -> new IllegalStateException("No 'Exert Frodo' option available for Moria Lake"));
    }

    private static Option chooseWestfarthingTarget(List<Option> options, SimulatedGame simulatedGame, String player) {
        PhysicalCard firstTarget = getTargets(options).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No target available for Westfarthing's ability"));
        boolean isTargetInHand = simulatedGame.getGame().getGameState().getHand(player).stream().anyMatch(card -> card.getCardId() == firstTarget.getCardId());

        if (isTargetInHand) {
            // choose whatever for Westfarthing's ability, will play everything anyway
            return options.getFirst();
        } else {
            // target in play, exert targeting, choose Frodo if possible
            Option frodoOption = findTargetWithFullName(options, FRODO);
            return frodoOption != null ? frodoOption : options.getFirst();
        }
    }

    private static Option chooseRivendellTerraceTarget(List<Option> options) {
        // choose whatever for Rivendell Terrace's ability, will play everything anyway
        return options.getFirst();
    }

    private static Option choosePillarsDiscardTarget(List<Option> options) {
        List<PhysicalCard> targets = getTargets(options);

        // discard in this order of preference
        List<Predicate<PhysicalCard>> discardPriority = List.of(
                hasCardType(CardType.COMPANION),
                hasFullName(NO_STRANGER),
                hasCardType(CardType.POSSESSION),
                // non-skirmish event
                card -> card.getBlueprint().getCardType() == CardType.EVENT && !card.getBlueprint().hasTimeword(Timeword.SKIRMISH)
        );

        for (Predicate<PhysicalCard> preferred : discardPriority) {
            for (PhysicalCard target : targets) {
                if (preferred.test(target)) {
                    return findOptionForTarget(options, target);
                }
            }
        }

        // discard whatever
        return options.getFirst();
    }
    private static Option choosePillarsHealTarget(List<Option> options) {
        Option option = findTargetByPriority(options, List.of(BOROMIR, ARAGORN));
        if (option == null) {
            throw new IllegalStateException("No valid target available for Pillars of the Kings heal");
        }
        return option;
    }

    private static Option chooseHobbitSwordTarget(List<Option> options) {
        // attach Hobbit Sword to Frodo if possible, if not choose Sam, if not choose whatever
        Option option = findTargetByPriority(options, List.of(FRODO, SAM));
        return option != null ? option : options.getFirst();
    }

    private static Option chooseArmorTarget(List<Option> options, SimulatedGame simulatedGame, String player) {
        Option aragornOption = requireTargetWithFullName(options, ARAGORN, "No valid target available for Armor");
        Option boromirOption = requireTargetWithFullName(options, BOROMIR, "No valid target available for Armor");

        int aragornVitality = getVitality(simulatedGame, ARAGORN, player);
        int boromirVitality = getVitality(simulatedGame, BOROMIR, player);

        if (aragornVitality > 1) {
            return aragornOption;
        } else if (boromirVitality > 1) {
            return boromirOption;
        } else {
            // if both have 1 vitality, choose Aragorn by default
            return aragornOption;
        }
    }

    private static Option chooseAthelasAttachTarget(List<Option> options) {
        return requireTargetWithFullName(options, ARAGORN, "Aragorn not found as a target for Athelas");
    }

    private static Option chooseAthelasHealTarget(List<Option> options) {
        Option option = findTargetByPriority(options, List.of(BOROMIR, LEGOLAS, ARAGORN, SAM, FRODO));
        if (option == null) {
            throw new IllegalStateException("No valid target available for Athelas heal");
        }
        return option;
    }

    private static Option chooseCoatOfMailTarget(List<Option> options) {
        return requireTargetWithFullName(options, BOROMIR, "Boromir not found as a target for Coat of Mail");
    }

    private static Option chooseGreatShieldTarget(List<Option> options, SimulatedGame simulatedGame, String player) {
        Option aragornOption = requireTargetWithFullName(options, ARAGORN, "No valid target available for Great Shield");
        Option boromirOption = requireTargetWithFullName(options, BOROMIR, "No valid target available for Great Shield");

        int aragornVitality = getVitality(simulatedGame, ARAGORN, player);
        int boromirVitality = getVitality(simulatedGame, BOROMIR, player);

        if (aragornVitality >= boromirVitality) {
            return aragornOption;
        } else {
            return boromirOption;
        }
    }

    private static boolean wantToActivateWestfarthing(SimulatedGame simulatedGame, String player) {
        int vitalitySaveThreshold = getVitalitySaveThreshold(simulatedGame, player);
        return getVitality(simulatedGame, FRODO, player) > 1 || (isInPlay(simulatedGame, SAM, player) && getVitality(simulatedGame, SAM, player) > vitalitySaveThreshold);
    }

    private static boolean wantToPlayRosie(SimulatedGame simulatedGame, String player) {
        // don't play if Sam is dead
        return simulatedGame.getGame().getGameState().getDeadPile(player).stream()
                .noneMatch(hasFullName(SAM));
    }

    private static boolean wantToPlayHobbitSword(SimulatedGame simulatedGame, String player) {
        // there must be a Hobbit companion without a Hobbit Sword to bear it
        return simulatedGame.getGame().getGameState().getInPlay().stream()
                .filter(hasCardType(CardType.COMPANION))
                .filter(card -> card.getBlueprint().getRace() == Race.HOBBIT)
                .filter(card -> card.getOwner().equals(player))
                .anyMatch(card -> simulatedGame.getGame().getGameState().getAttachedCards(card).stream().noneMatch(hasFullName(HOBBIT_SWORD)));
    }

    private static boolean wantToPlayArmor(SimulatedGame simulatedGame, String player) {
        FotrStarterDeck opponentDeck = getOpponentDeck(simulatedGame, player);
        // only play against Uruks
        return opponentDeck == FotrStarterDeck.ARAGORN_STARTER;
    }

    private static boolean wantToPlayCoatOfMail(SimulatedGame simulatedGame, String player) {
        FotrStarterDeck opponentDeck = getOpponentDeck(simulatedGame, player);

        // always play against Moria
        if (opponentDeck == FotrStarterDeck.GANDALF_STARTER) {
            return true;
        }

        // against Uruks, Armor is better, we want to save space for that
        // Armor already in play, we can play Coat of Mail as well
        if (isInPlay(simulatedGame, ARMOR, player)) {
            return true;
        }
        // both Aragorn and Boromir in play, if Coat of Mail gets played, we still have space for Armor
        return isInPlay(simulatedGame, ARAGORN, player) && isInPlay(simulatedGame, BOROMIR, player);
    }

    private static boolean wantToPlayGreatShield(SimulatedGame simulatedGame, String player) {
        FotrStarterDeck opponentDeck = getOpponentDeck(simulatedGame, player);
        // only play against Moria
        return opponentDeck == FotrStarterDeck.GANDALF_STARTER;
    }

    private static boolean wantToPlayPathfinder(SimulatedGame simulatedGame, String player) {
        FotrStarterDeck opponentDeck = getOpponentDeck(simulatedGame, player);

        // if in mirror match, never play, save it for regroup to play with no value
        if (opponentDeck == FotrStarterDeck.ARAGORN_STARTER) {
            return false;
        }

        int currentSiteNumber = simulatedGame.getGame().getGameState().getCurrentSiteNumber();
        // do not use at site 4, as site 5 is always the Bridge
        if (currentSiteNumber == 4) {
            return false;
        }
        // do not use at sites 5 and 7, as waiting for regroup after moving maps more impactful site
        if (currentSiteNumber == 5 || currentSiteNumber == 7) {
            return false;
        }

        PhysicalCard nextSite = simulatedGame.getGame().getGameState().getSite(currentSiteNumber + 1);
        // if next site is not played yet or next site belongs to the opponent, play Pathfinder
        if (nextSite == null || !nextSite.getOwner().equals(player)) {
            return true;
        }

        // does not help, save it for regroup to maybe play it with some effect
        return false;
    }

    private static boolean wantToActivateSam(SimulatedGame simulatedGame, String player) {
        int burdens = simulatedGame.getGame().getGameState().getBurdens();
        // critical amount, always activate
        if (burdens >= 8) {
            return true;
        }

        int vitalitySaveThreshold = getVitalitySaveThreshold(simulatedGame, player);

        // if Sam would not die from one more hit, and there are more than 3 burdens, activate Sam's ability to reduce burdens
        return getVitality(simulatedGame, SAM, player) > vitalitySaveThreshold && burdens > 3;
    }

    private static boolean wantToActivateRosie(SimulatedGame simulatedGame, String player) {
        // if Sam is not in play, do not activate Rosie
        if (!isInPlay(simulatedGame, SAM, player)) {
            return false;
        }

        // if Sam is wounded and site number is 3 or more (do not want to waste heal before the first sanctuary), activate Rosie's ability to heal Sam
        return getVitality(simulatedGame, SAM, player) < 4 && simulatedGame.getGame().getGameState().getCurrentSiteNumber() >= 3;
    }

    private static boolean wantToActivateAthelas(SimulatedGame simulatedGame, String player) {
        int currentSiteNumber = simulatedGame.getGame().getGameState().getCurrentSiteNumber();
        // never activate before the first sanctuary, save for later
        if (currentSiteNumber < 3) {
            return false;
        }

        // do not heal hobbits, their vitality is expendable, we want to save Athelas for Aragorn, Boromir and Legolas
        return hasWoundedCompanion(simulatedGame, player, card -> card.getBlueprint().getRace() != Race.HOBBIT);
    }

    private static boolean wantToActivatePillars(SimulatedGame simulatedGame, String player) {
        return hasWoundedCompanion(simulatedGame, player, card -> card.getBlueprint().getCulture() == Culture.GONDOR);
    }
}
