package com.gempukku.lotro.bots.v2.fotr_starter.fellowship;

import com.gempukku.lotro.bots.v2.decision.option.*;
import com.gempukku.lotro.bots.v2.decision.request.DecisionRequest;
import com.gempukku.lotro.bots.v2.fotr_starter.FotrStarterDeck;
import com.gempukku.lotro.bots.v2.fotr_starter.Plan;
import com.gempukku.lotro.bots.v2.fotr_starter.SimplePlan;
import com.gempukku.lotro.bots.v2.fotr_starter.utility.DeadCardEvaluator;
import com.gempukku.lotro.bots.v2.simulation.SimulatedGame;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;
import org.apache.commons.lang3.NotImplementedException;

import java.util.*;
import java.util.function.BooleanSupplier;

import static com.gempukku.lotro.bots.v2.fotr_starter.fellowship.CardPredicates.*;
import static com.gempukku.lotro.bots.v2.fotr_starter.fellowship.GameQueries.*;
import static com.gempukku.lotro.bots.v2.fotr_starter.fellowship.OptionFinders.*;

public class GandalfFellowshipPhasePlanner {
    private static final String FRODO = "Frodo, Son of Drogo";
    private static final String GIMLI = "Gimli, Dwarf of Erebor";
    private static final String GANDALF = "Gandalf, The Grey Wizard";
    private static final String LEGOLAS = "Legolas, Prince of Mirkwood";
    private static final String BOROMIR = "Boromir, Son of Denethor";
    private static final String BARLIMAN_BUTTERBUR = "Barliman Butterbur, Prancing Pony Proprietor";
    private static final String WESTFARTHING = "Westfarthing";
    private static final String RIVENDELL_TERRACE = "Rivendell Terrace";
    private static final String MORIA_LAKE = "Moria Lake";
    private static final String HOBBIT_SWORD = "Hobbit Sword";
    private static final String SLEEP_CARADHRAS = "Sleep, Caradhras";
    private static final String TREACHERY_DEEPER_THAN_YOU_KNOW = "Treachery Deeper Than You Know";
    private static final String SORRY_ABOUT_EVERYTHING = "Sorry About Everything";
    private static final String MYSTERIOUS_WIZARD = "Mysterious Wizard";
    private static final String INTIMIDATE = "Intimidate";
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
                // check Gimli's ability first because of twilight restriction, activate if there are at least 2 dead cards in hand
                rule(findActivate(options, hasFullName(GIMLI)), () -> wantToActivateGimli(simulatedGame, player)),
                // if we can discard a companion to heal, we should do that to get rid of card in hand and get value
                rule(findOptionOfType(options, DiscardCardToHealOption.class)),
                // activate Gandalf's ability before playing companions, if the exert is not too punishing
                rule(findActivate(options, hasFullName(GANDALF)), () -> wantToActivateGandalf(simulatedGame, player)),
                // activate Westfarthing's ability if Frodo can be exerted, before playing companions or allies
                rule(findActivate(options, hasFullName(WESTFARTHING)), () -> wantToActivateWestfarthing(simulatedGame, player)),
                // activate Rivendell Terrace before playing companions
                rule(findActivate(options, hasFullName(RIVENDELL_TERRACE))),
                // always play more companions
                rule(findPlay(options, hasCardType(CardType.COMPANION))),
                // always play more allies
                rule(findPlay(options, hasCardType(CardType.ALLY))),
                // always play Hobbit Sword if available
                rule(findPlay(options, hasFullName(HOBBIT_SWORD))),
                // play Sleep if there are enemy conditions in play
                rule(findPlay(options, hasFullName(SLEEP_CARADHRAS)), () -> wantToPlaySleep(simulatedGame, player)),
                // play Treachery Deeper Than You Know if available
                rule(findPlay(options, hasFullName(TREACHERY_DEEPER_THAN_YOU_KNOW))),
                // play Sorry About Everything if there is a burden to be removed
                rule(findPlay(options, hasFullName(SORRY_ABOUT_EVERYTHING)), () -> wantToPlaySorryAboutEverything(simulatedGame, player)),
                // activate Barliman Butterbur's ability if there is good target in the discard pile
                rule(findActivate(options, hasFullName(BARLIMAN_BUTTERBUR)), () -> wantToActivateBarliman(simulatedGame, player))
        );

        // conditions are evaluated lazily, only for rules whose option is available
        return rules.stream()
                .filter(rule -> rule.option() != null && rule.condition().getAsBoolean())
                .map(FellowshipRule::option)
                .findFirst()
                .orElse(passOption);
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

    private static Option chooseFollowUpOption(DecisionRequest decision, SimulatedGame simulatedGame, String player) {
        // choose targets etc. for the fellowship actions and for the move responses
        List<Option> options = decision.getOptions();

        if (decision.isChooseOptionForCardDecision() && sourceHasFullName(decision, MORIA_LAKE)) {
            return chooseMoriaLakeChoice(options);
        } else if (decision.isChooseTargetForCardDecision() && sourceHasFullName(decision, GIMLI)) {
            return chooseGimliTarget(options, simulatedGame, player);
        } else if (decision.isChooseTargetForCardDecision() && sourceHasFullName(decision, GANDALF)) {
            return chooseGandalfTarget(options);
        } else if (decision.isChooseTargetForCardDecision() && sourceHasFullName(decision, WESTFARTHING)) {
            return chooseWestfarthingTarget(options, simulatedGame, player);
        } else if (decision.isChooseTargetForCardDecision() && sourceHasFullName(decision, RIVENDELL_TERRACE)) {
            return chooseRivendellTerraceTarget(options);
        } else if (decision.isChooseTargetForCardDecision() && sourceHasFullName(decision, HOBBIT_SWORD)) {
            return chooseHobbitSwordTarget(options);
        } else if (decision.isHandReveal()) {
            // always pass when opponent's hand is revealed, nothing else to do
            return options.getFirst();
        } else if (decision.isChooseArbitraryTargetForCardDecision() && sourceHasFullName(decision, BARLIMAN_BUTTERBUR)) {
            return chooseBarlimanTarget(options, simulatedGame, player);
        } else {
            throw new NotImplementedException("Unsupported decision: " + decision);
        }
    }

    private static Option chooseMoriaLakeChoice(List<Option> options) {
        // choose 'Exert Frodo' option for Moria Lake
        return findChoice(options, EXERT_FRODO_CHOICE)
                .orElseThrow(() -> new IllegalStateException("No 'Exert Frodo' option available for Moria Lake"));
    }

    private static Option chooseGimliTarget(List<Option> options, SimulatedGame simulatedGame, String player) {
        // choose one of the dead cards in hand for Gimli's ability
        PhysicalCard target = chooseTargetForGimli(getTargets(options), simulatedGame, player);
        Option targetOption = findOptionForTarget(options, target);
        if (targetOption == null) {
            throw new IllegalStateException("Target for Gimli not found");
        }
        return targetOption;
    }

    private static Option chooseGandalfTarget(List<Option> options) {
        // choose whatever for Gandalf's ability, the options are the same
        return options.getFirst();
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

    private static Option chooseHobbitSwordTarget(List<Option> options) {
        // attach Hobbit Sword to Frodo
        Option frodoOption = findTargetWithFullName(options, FRODO);
        if (frodoOption == null) {
            throw new IllegalStateException("No Frodo target available for Hobbit Sword");
        }
        return frodoOption;
    }

    private static Option chooseBarlimanTarget(List<Option> options, SimulatedGame simulatedGame, String player) {
        // choose the best event to return
        LotroCardBlueprint target = chooseTargetForBarliman(getArbitraryTargets(options), simulatedGame, player);
        return findArbitraryTarget(options, target.getFullName())
                .orElseThrow(() -> new IllegalStateException("Target for Barliman Butterbur not found: " + target.getFullName()));
    }

    private static List<PhysicalCard> chooseTargetsForMoriaLake(List<PhysicalCard> possibleTargets, SimulatedGame simulatedGame, String player) {
        // choose two to exert
        List<PhysicalCard> tbr = new ArrayList<>();

        Map<PhysicalCard, Integer> vitalityMap = new HashMap<>();
        for (PhysicalCard target : possibleTargets) {
            int vitality = simulatedGame.getGame().getModifiersQuerying().getVitality(simulatedGame.getGame(), target);
            vitalityMap.put(target, vitality);
        }

        // whom losing hurts the least
        List<String> exertPriority = List.of(GIMLI, LEGOLAS, BOROMIR, GANDALF);

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

    private static PhysicalCard chooseTargetForGimli(List<PhysicalCard> possibleTargets, SimulatedGame simulatedGame, String player) {
        List<PhysicalCard> deadCards = DeadCardEvaluator.getUnplayableCardsInHand(simulatedGame.getGame(), player);
        // choose the first dead card in hand
        for (PhysicalCard target : possibleTargets) {
            for (PhysicalCard deadCard : deadCards) {
                if (deadCard.getCardId() == target.getCardId()) {
                    return target;
                }
            }
        }

        // fallback
        return possibleTargets.getFirst();
    }

    private static LotroCardBlueprint chooseTargetForBarliman(List<LotroCardBlueprint> possibleTargets, SimulatedGame simulatedGame, String player) {
        LotroCardBlueprint sleep = findByFullName(possibleTargets, SLEEP_CARADHRAS);
        LotroCardBlueprint mysteriousWizard = findByFullName(possibleTargets, MYSTERIOUS_WIZARD);
        LotroCardBlueprint intimidate = findByFullName(possibleTargets, INTIMIDATE);

        // return Sleep if 2 or more enemy conditions in play
        // else return Mysterious Wizard if in discard pile
        // else return Intimidate if in discard pile
        // else return whatever

        if (sleep != null && countEnemyConditionsInPlay(simulatedGame, player) >= 2) {
            return sleep;
        } else if (mysteriousWizard != null) {
            return mysteriousWizard;
        } else if (intimidate != null) {
            return intimidate;
        } else {
            return possibleTargets.getFirst();
        }
    }

    private static boolean wantToActivateBarliman(SimulatedGame simulatedGame, String player) {
        // never use if Gandalf is in the dead pile
        boolean gandalfDead = simulatedGame.getGame().getGameState().getDeadPile(player).stream()
                .anyMatch(deadCard -> deadCard.getBlueprint().getFullName().equals(GANDALF));
        if (gandalfDead) {
            return false;
        }

        // check if there is a good target in the discard pile
        boolean wantSleep = wantToPlaySleep(simulatedGame, player);

        return simulatedGame.getGame().getGameState().getDiscard(player).stream()
                .map(card -> card.getBlueprint().getFullName())
                .anyMatch(name -> name.equals(MYSTERIOUS_WIZARD) || name.equals(INTIMIDATE) || (name.equals(SLEEP_CARADHRAS) && wantSleep));
    }

    private static boolean wantToActivateWestfarthing(SimulatedGame simulatedGame, String player) {
        return getVitality(simulatedGame, FRODO, player) > 1;
    }

    private static boolean wantToPlaySorryAboutEverything(SimulatedGame simulatedGame, String player) {
        // do not exhaust Frodo to remove a burden
        return simulatedGame.getGame().getGameState().getPlayerBurdens(player) > 0 && getVitality(simulatedGame, FRODO, player) >= 3;
    }

    private static boolean wantToPlaySleep(SimulatedGame simulatedGame, String player) {
        // check if there are any enemy conditions in play that we want to discard with Sleep
        return countEnemyConditionsInPlay(simulatedGame, player) > 0;
    }

    private static long countEnemyConditionsInPlay(SimulatedGame simulatedGame, String player) {
        return simulatedGame.getGame().getGameState().getInPlay().stream()
                .filter(card -> card.getBlueprint().getCardType() == CardType.CONDITION && !card.getOwner().equals(player) && card.getBlueprint().getSide() == Side.SHADOW)
                .count();
    }

    private static boolean wantToActivateGandalf(SimulatedGame simulatedGame, String player) {
        FotrStarterDeck opponentDeck = getOpponentDeck(simulatedGame, player);
        int gandalfVitality = getVitality(simulatedGame, GANDALF, player);

        if (gandalfVitality == 3 && opponentDeck == FotrStarterDeck.ARAGORN_STARTER) {
            // do not want to drop to 2 vitality against Uruk shadow
            return false;
        } else if (gandalfVitality == 2 && opponentDeck == FotrStarterDeck.GANDALF_STARTER) {
            // do not want to drop to 1 vitality against Moria shadow
            return false;
        }

        return true;
    }

    private static boolean wantToActivateGimli(SimulatedGame simulatedGame, String player) {
        // one card can be discarded to reconcile at the end of turn
        return DeadCardEvaluator.getUnplayableCardsInHand(simulatedGame.getGame(), player).size() >= 2;
    }

    private static LotroCardBlueprint findByFullName(List<LotroCardBlueprint> blueprints, String fullName) {
        return blueprints.stream()
                .filter(blueprint -> blueprint.getFullName().equals(fullName))
                .findFirst()
                .orElse(null);
    }
}
