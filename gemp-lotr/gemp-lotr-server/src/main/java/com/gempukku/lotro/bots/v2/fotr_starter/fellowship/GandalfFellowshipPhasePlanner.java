package com.gempukku.lotro.bots.v2.fotr_starter.fellowship;

import com.gempukku.lotro.bots.v2.decision.option.*;
import com.gempukku.lotro.bots.v2.decision.request.DecisionRequest;
import com.gempukku.lotro.bots.v2.fotr_starter.DeckRecognizer;
import com.gempukku.lotro.bots.v2.fotr_starter.FotrStarterDeck;
import com.gempukku.lotro.bots.v2.fotr_starter.Plan;
import com.gempukku.lotro.bots.v2.fotr_starter.SimplePlan;
import com.gempukku.lotro.bots.v2.fotr_starter.utility.DeadCardEvaluator;
import com.gempukku.lotro.bots.v2.simulation.SimulatedGame;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;
import org.apache.commons.lang3.NotImplementedException;

import java.util.*;

public class GandalfFellowshipPhasePlanner {
    public static Plan getPlan(DefaultLotroGame game, String player) {
        SimulatedGame simulatedGame = new SimulatedGame(game);
        List<List<Option>> actionsToTake = new ArrayList<>();
        DecisionRequest nextDecision = simulatedGame.getDecisionRequest(player);

        while (nextDecision != null && simulatedGame.getGame().getGameState().getCurrentPhase() == Phase.FELLOWSHIP) {
            List<Option> options = nextDecision.getOptions();
            Option optionToTake;
            List<Option> multiChoiceOptions = new ArrayList<>();

            if (nextDecision.isFellowshipPhaseDecision()) {
                // play cards or activate abilities

                Option activateGimliOption = options.stream()
                        .filter(option -> option instanceof ActivateCardOption activateCardOption && activateCardOption.getCard().getBlueprint().getTitle().equals("Gimli"))
                        .findFirst()
                        .orElse(null);
                Option discardCompanionToHealOption = options.stream()
                        .filter(option -> option instanceof DiscardCardToHealOption)
                        .findFirst()
                        .orElse(null);
                Option activateGandalfOption = options.stream()
                        .filter(option -> option instanceof ActivateCardOption activateCardOption && activateCardOption.getCard().getBlueprint().getTitle().equals("Gandalf"))
                        .findFirst()
                        .orElse(null);
                Option activateWestfarthingOption = options.stream()
                        .filter(option -> option instanceof ActivateCardOption activateCardOption && activateCardOption.getCard().getBlueprint().getFullName().equals("Westfarthing"))
                        .findFirst()
                        .orElse(null);
                Option activateRivendellTerrace = options.stream()
                        .filter(option -> option instanceof ActivateCardOption activateCardOption && activateCardOption.getCard().getBlueprint().getFullName().equals("Rivendell Terrace"))
                        .findFirst()
                        .orElse(null);
                Option playCompanionOption = options.stream()
                        .filter(option -> option instanceof PlayCardOption playCardOption && playCardOption.getCard().getBlueprint().getCardType() == CardType.COMPANION)
                        .findFirst()
                        .orElse(null);
                Option playAllyOption = options.stream()
                        .filter(option -> option instanceof PlayCardOption playCardOption && playCardOption.getCard().getBlueprint().getCardType() == CardType.ALLY)
                        .findFirst()
                        .orElse(null);
                Option playHobbitSword = options.stream()
                        .filter(option -> option instanceof PlayCardOption playCardOption && playCardOption.getCard().getBlueprint().getFullName().equals("Hobbit Sword"))
                        .findFirst()
                        .orElse(null);
                Option playSleep = options.stream()
                        .filter(option -> option instanceof PlayCardOption playCardOption && playCardOption.getCard().getBlueprint().getFullName().equals("Sleep, Caradhras"))
                        .findFirst()
                        .orElse(null);
                Option playTreachery = options.stream()
                        .filter(option -> option instanceof PlayCardOption playCardOption && playCardOption.getCard().getBlueprint().getFullName().equals("Treachery Deeper Than You Know"))
                        .findFirst()
                        .orElse(null);
                Option playSorryAboutEverything = options.stream()
                        .filter(option -> option instanceof PlayCardOption playCardOption && playCardOption.getCard().getBlueprint().getFullName().equals("Sorry About Everything"))
                        .findFirst()
                        .orElse(null);
                Option activateBarlimanOption = options.stream()
                        .filter(option -> option instanceof ActivateCardOption activateCardOption && activateCardOption.getCard().getBlueprint().getTitle().equals("Barliman Butterbur"))
                        .findFirst()
                        .orElse(null);

                Option passOption = options.stream()
                        .filter(option -> option instanceof PassOption)
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("No pass option available in fellowship phase decision"));

                if (activateGimliOption != null && wantToActivateGimli(simulatedGame, player)) {
                    // check Gimli's ability first because of twilight restriction, activate if there are at least 2 dead cards in hand
                    optionToTake = activateGimliOption;
                } else if (discardCompanionToHealOption != null) {
                    // if we can discard a companion to heal, we should do that to get rid of card in hand and get value
                    optionToTake = discardCompanionToHealOption;
                } else if (activateGandalfOption != null && wantToActivateGandalf(simulatedGame, player)) {
                    // activate Gandalf's ability before playing companions, if the exert is not too punishing
                    optionToTake = activateGandalfOption;
                } else if (activateWestfarthingOption != null && wantToActivateWestfarthing(simulatedGame, player)) {
                    // activate Westfarthing's ability if Frodo can be exerted, before playing companions or allies
                    optionToTake = activateWestfarthingOption;
                } else if (activateRivendellTerrace != null) {
                    // activate Rivendell Terrace before playing companions
                    optionToTake = activateRivendellTerrace;
                } else if (playCompanionOption != null) {
                    // always play more companions
                    optionToTake = playCompanionOption;
                } else if (playAllyOption != null) {
                    // always play more allies
                    optionToTake = playAllyOption;
                } else if (playHobbitSword != null) {
                    // always play Hobbit Sword if available
                    optionToTake = playHobbitSword;
                } else if (playSleep != null && wantToPlaySleep(simulatedGame, player)) {
                    // play Sleep if there are enemy conditions in play
                    optionToTake = playSleep;
                } else if (playTreachery != null) {
                    // play Treachery Deeper Than You Know if available
                    optionToTake = playTreachery;
                } else if (playSorryAboutEverything != null && wantToPlaySorryAboutEverything(simulatedGame, player)) {
                    // play Sorry About Everything if there is a burden to be removed
                    optionToTake = playSorryAboutEverything;
                } else if (activateBarlimanOption != null && wantToActivateBarliman(simulatedGame, player)) {
                    // activate Barliman Butterbur's ability if there is good target in the discard pile
                    optionToTake = activateBarlimanOption;
                } else {
                    optionToTake = passOption;
                }
            } else {
                // choose targets etc. for the above actions and for the move responses

                if (nextDecision.isChooseOptionForCardDecision() && nextDecision.getSource().getBlueprint().getFullName().equals("Moria Lake")) {
                    // choose 'Exert Frodo' option for Moria Lake
                    optionToTake = nextDecision.getOptions().stream()
                            .filter(option -> option instanceof ChooseOptionForCardOption chooseOptionForCardOption && chooseOptionForCardOption.getChoice().equals("Exert Frodo"))
                            .findFirst()
                            .orElseThrow(() -> new IllegalStateException("No 'Exert Frodo' option available for Moria Lake"));
                } else if (nextDecision.isChooseTargetForCardDecision() && nextDecision.getSource().getBlueprint().getFullName().equals("Moria Lake")) {
                    // exerting Frodo was not possible, need to exert two others
                    List<PhysicalCard> possibleTargets = nextDecision.getOptions().stream()
                            .filter(option -> option instanceof ChooseTargetForCardOption)
                            .map(option -> ((ChooseTargetForCardOption) option).getTarget())
                            .toList();
                    List<PhysicalCard> targetsToExert = chooseTargetsForMoriaLake(possibleTargets, simulatedGame, player);
                    // choosing two
                    multiChoiceOptions = nextDecision.getOptions().stream()
                            .filter(option -> option instanceof ChooseTargetForCardOption chooseTargetForCardOption && targetsToExert.stream().anyMatch(target -> target.getCardId() == chooseTargetForCardOption.getTarget().getCardId()))
                            .toList();
                    optionToTake = null;
                } else if (nextDecision.isChooseTargetForCardDecision() && nextDecision.getSource().getBlueprint().getTitle().equals("Gimli")) {
                    // choose one of the dead cards in hand for Gimli's ability
                    PhysicalCard target = chooseTargetForGimli(nextDecision.getOptions().stream()
                                    .filter(option -> option instanceof ChooseTargetForCardOption)
                                    .map(option -> ((ChooseTargetForCardOption) option).getTarget())
                                    .toList(),
                            simulatedGame, player);
                    optionToTake = nextDecision.getOptions().stream()
                            .filter(option -> option instanceof ChooseTargetForCardOption chooseTargetForCardOption && chooseTargetForCardOption.getTarget().getCardId() == target.getCardId())
                            .findFirst()
                            .orElseThrow(() -> new IllegalStateException("Target for Gimli not found"));
                } else if (nextDecision.isChooseTargetForCardDecision() && nextDecision.getSource().getBlueprint().getTitle().equals("Gandalf")) {
                    // choose whatever for Gandalf's ability, the options are the same
                    optionToTake = nextDecision.getOptions().getFirst();
                } else if (nextDecision.isChooseTargetForCardDecision() && nextDecision.getSource().getBlueprint().getFullName().equals("Westfarthing")) {
                    PhysicalCard firstTarget = nextDecision.getOptions().stream()
                            .filter(option -> option instanceof ChooseTargetForCardOption)
                            .map(option -> ((ChooseTargetForCardOption) option).getTarget())
                            .findFirst()
                            .orElseThrow(() -> new IllegalStateException("No target available for Westfarthing's ability"));
                    boolean isTargetInHand = simulatedGame.getGame().getGameState().getHand(player).stream().anyMatch(card -> card.getCardId() == firstTarget.getCardId());

                    if (isTargetInHand) {
                        // choose whatever for Westfarthing's ability, will play everything anyway
                        optionToTake = nextDecision.getOptions().getFirst();
                    } else {
                        // target in play, exert targeting, choose Frodo if possible
                        optionToTake = nextDecision.getOptions().stream()
                                .filter(option -> option instanceof ChooseTargetForCardOption chooseTargetForCardOption && chooseTargetForCardOption.getTarget().getBlueprint().getTitle().equals("Frodo"))
                                .findFirst()
                                .orElse(nextDecision.getOptions().getFirst());
                    }
                } else if (nextDecision.isChooseTargetForCardDecision() && nextDecision.getSource().getBlueprint().getFullName().equals("Rivendell Terrace")) {
                    // choose whatever for Rivendell Terrace's ability, will play everything anyway
                    optionToTake = nextDecision.getOptions().getFirst();
                } else if (nextDecision.isChooseTargetForCardDecision() && nextDecision.getSource().getBlueprint().getFullName().equals("Hobbit Sword")) {
                    // attach Hobbit Sword to Frodo
                    optionToTake = nextDecision.getOptions().stream()
                            .filter(option -> option instanceof ChooseTargetForCardOption chooseTargetForCardOption && chooseTargetForCardOption.getTarget().getBlueprint().getTitle().equals("Frodo"))
                            .findFirst()
                            .orElseThrow(() -> new IllegalStateException("No Frodo target available for Hobbit Sword"));
                } else if (nextDecision.isHandReveal()) {
                    // always pass when opponent's hand is revealed, nothing else to do
                    optionToTake = nextDecision.getOptions().getFirst();
                } else if (nextDecision.isChooseArbitraryTargetForCardDecision() && nextDecision.getSource().getBlueprint().getTitle().equals("Barliman Butterbur")) {
                    // choose the best event to return
                    LotroCardBlueprint target = chooseTargetForBarliman(nextDecision.getOptions().stream()
                            .filter(option -> option instanceof ChooseArbitraryTargetForCardOption)
                            .map(option -> ((ChooseArbitraryTargetForCardOption) option).getBlueprint())
                            .toList(),
                            simulatedGame, player);
                    optionToTake = nextDecision.getOptions().stream()
                            .filter(option -> option instanceof ChooseArbitraryTargetForCardOption chooseArbitraryTargetForCardOption && chooseArbitraryTargetForCardOption.getBlueprint().getFullName().equals(target.getFullName()))
                            .findFirst()
                            .orElseThrow(() -> new IllegalStateException("Target for Barliman Butterbur not found: " + target.getFullName()));
                } else {
                    throw new NotImplementedException("Unsupported decision: " + nextDecision);
                }
            }

            if (optionToTake != null) {
                actionsToTake.add(List.of(optionToTake));
                simulatedGame.makeDecision(player, optionToTake.getAnswerForGame());
            } else {
                actionsToTake.add(multiChoiceOptions);
                simulatedGame.makeDecision(player, nextDecision.combineSelected(multiChoiceOptions));
            }
            nextDecision = simulatedGame.getDecisionRequest(player);
        }

        return new SimplePlan(actionsToTake);
    }

    private static List<PhysicalCard> chooseTargetsForMoriaLake(List<PhysicalCard> possibleTargets, SimulatedGame simulatedGame, String player) {
        // choose two to exert
        List<PhysicalCard> tbr = new ArrayList<>();

        FotrStarterDeck opponentDeck = DeckRecognizer.recognize(simulatedGame.getGame().getGameState().getLotroDeck(simulatedGame.getGame().getGameState().getOpponent(player)));
        Map<PhysicalCard, Integer> vitalityMap = new HashMap<>();
        for (PhysicalCard target : possibleTargets) {
            int vitality = simulatedGame.getGame().getModifiersQuerying().getVitality(simulatedGame.getGame(), target);
            vitalityMap.put(target, vitality);
        }

        // whom losing hurts the least
        List<String> exertPriority = List.of("Gimli", "Legolas", "Boromir", "Gandalf");

        // need more than 3 vitality to exert against Uruks, more than 2 against Moria
        int vitalitySaveThreshold = opponentDeck == FotrStarterDeck.ARAGORN_STARTER ? 3 : 2;
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
                    .sorted(Comparator.comparingInt(a -> exertPriority.indexOf(a.getBlueprint().getTitle())))
                    .toList();
            tbr.add(sortedTargets.getFirst());
            tbr.add(sortedTargets.get(1));
        }

        while (tbr .size() < 2) {
            // fill with the highest exert priority
            List<PhysicalCard> sortedTargets = possibleTargets.stream()
                    .filter(target -> !tbr.contains(target))
                    .sorted(Comparator.comparingInt(a -> exertPriority.indexOf(a.getBlueprint().getTitle())))
                    .toList();
            tbr.add(sortedTargets.getFirst());
        }

        return tbr;
    }

    private static PhysicalCard chooseTargetForGimli(List<PhysicalCard> possibleTargets, SimulatedGame simulatedGame, String player) {
        List<PhysicalCard> deadCards = DeadCardEvaluator.getDeadCards(simulatedGame.getGame(), player);
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
        LotroCardBlueprint sleep = possibleTargets.stream()
                .filter(card -> card.getFullName().equals("Sleep, Caradhras"))
                .findFirst()
                .orElse(null);
        LotroCardBlueprint mysteriousWizard = possibleTargets.stream()
                .filter(card -> card.getFullName().equals("Mysterious Wizard"))
                .findFirst()
                .orElse(null);
        LotroCardBlueprint intimidate = possibleTargets.stream()
                .filter(card -> card.getFullName().equals("Intimidate"))
                .findFirst()
                .orElse(null);

        // return Sleep if 2 or more enemy conditions in play
        // else return Mysterious Wizard if in discard pile
        // else return Intimidate if in discard pile
        // else return whatever

        if (sleep != null && simulatedGame.getGame().getGameState().getInPlay().stream()
                .filter(card -> card.getBlueprint().getCardType() == CardType.CONDITION && !card.getOwner().equals(player)).count() >= 2) {
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
        boolean gandalfDead = false;
        for (PhysicalCard deadCard : simulatedGame.getGame().getGameState().getDeadPile(player)) {
            if (deadCard.getBlueprint().getTitle().equals("Gandalf")) {
                gandalfDead = true;
            }
        }
        if (gandalfDead) {
            return false;
        }

        // check if there is a good target in the discard pile
        boolean goodTargetInDiscard = false;
        List<PhysicalCard> discardPile = (List<PhysicalCard>) simulatedGame.getGame().getGameState().getDiscard(player);
        boolean wantSleep = wantToPlaySleep(simulatedGame, player);

        for (PhysicalCard physicalCard : discardPile) {
            if (physicalCard.getBlueprint().getFullName().equals("Mysterious Wizard")) {
                goodTargetInDiscard = true;
            } else if (physicalCard.getBlueprint().getFullName().equals("Intimidate")) {
                goodTargetInDiscard = true;
            } else if (physicalCard.getBlueprint().getFullName().equals("Sleep, Caradhras") && wantSleep) {
                goodTargetInDiscard = true;
            }
        }

        return goodTargetInDiscard;
    }

    private static boolean wantToActivateWestfarthing(SimulatedGame simulatedGame, String player) {
        int frodoVitality = simulatedGame.getGame().getModifiersQuerying().getVitality(simulatedGame.getGame(), simulatedGame.getGame().getGameState().getInPlay().stream().filter(card -> card.getBlueprint().getTitle().equals("Frodo") && card.getOwner().equals(player)).findFirst().orElseThrow(() -> new IllegalStateException("Frodo not in play")));
        return frodoVitality > 1;
    }

    private static boolean wantToPlaySorryAboutEverything(SimulatedGame simulatedGame, String player) {
        // do not exhaust Frodo to remove a burden
        int frodoVitality = simulatedGame.getGame().getModifiersQuerying().getVitality(simulatedGame.getGame(), simulatedGame.getGame().getGameState().getInPlay().stream().filter(card -> card.getBlueprint().getTitle().equals("Frodo") && card.getOwner().equals(player)).findFirst().orElseThrow(() -> new IllegalStateException("Frodo not in play")));
        return simulatedGame.getGame().getGameState().getPlayerBurdens(player) > 0 && frodoVitality >= 3;
    }

    private static boolean wantToPlaySleep(SimulatedGame simulatedGame, String player) {
        // check if there are any enemy conditions in play that we want to discard with Sleep
        return simulatedGame.getGame().getGameState().getInPlay().stream()
                .anyMatch(card -> card.getBlueprint().getCardType() == CardType.CONDITION && !card.getOwner().equals(player));
    }

    private static boolean wantToActivateGandalf(SimulatedGame simulatedGame, String player) {
        FotrStarterDeck opponentDeck = DeckRecognizer.recognize(simulatedGame.getGame().getGameState().getLotroDeck(simulatedGame.getGame().getGameState().getOpponent(player)));
        int gandalfVitality = simulatedGame.getGame().getModifiersQuerying().getVitality(simulatedGame.getGame(), simulatedGame.getGame().getGameState().getInPlay().stream().filter(card -> card.getBlueprint().getTitle().equals("Gandalf") && card.getOwner().equals(player)).findFirst().orElseThrow(() -> new IllegalStateException("Gandalf not in play")));

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
        return DeadCardEvaluator.getDeadCards(simulatedGame.getGame(), player).size() >= 2;
    }
}
