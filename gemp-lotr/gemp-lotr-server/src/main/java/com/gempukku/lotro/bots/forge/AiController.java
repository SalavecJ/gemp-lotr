package com.gempukku.lotro.bots.forge;

import com.gempukku.lotro.bots.BotService;
import com.gempukku.lotro.bots.forge.controller.*;
import com.gempukku.lotro.bots.forge.plan.BetweenTurnsPlan;
import com.gempukku.lotro.bots.forge.plan.FellowshipPhasePlan;
import com.gempukku.lotro.bots.forge.plan.ShadowPhasePlan;
import com.gempukku.lotro.bots.forge.utils.StartingFellowshipUtil;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.SitesBlock;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.modifiers.ModifierFlag;
import com.gempukku.lotro.logic.timing.RuleUtils;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

public class AiController {
    private final String aiPlayerName;
    private final boolean printDebugMessages;

    private BetweenTurnsPlan betweenTurnsPlan = null;
    private FellowshipPhasePlan fellowshipPhasePlan = null;
    private ShadowPhasePlan shadowPhasePlan = null;

    public AiController(String aiPlayerName, boolean printDebugMessages) {
        this.aiPlayerName = aiPlayerName;
        this.printDebugMessages = printDebugMessages;
    }

    public void cleanUpAfterGame() {
        System.out.println("CLEANUP");
        betweenTurnsPlan = null;
        fellowshipPhasePlan = null;
        shadowPhasePlan = null;
    }

    private static void printSeparator() {
        System.out.println("----------");
    }

    public int chooseBurdensToBid(LotroGame game) {
        if (printDebugMessages) {
            printSeparator();
            System.out.println("Choosing number to bid");
        }

        // Bid randomly less than half of resistance
        // TODO bid more if starting with Sam SoH
        int resistance = 10;
        if (BotService.staticLibrary != null) {
            try {
                resistance = BotService.staticLibrary.getLotroCardBlueprint(game.getGameState().getLotroDeck(aiPlayerName).getRingBearer()).getResistance();
                if (printDebugMessages) {
                    System.out.println("Ring-bearer found: " + BotService.staticLibrary.getLotroCardBlueprint(game.getGameState().getLotroDeck(aiPlayerName).getRingBearer()).getFullName());
                }
            } catch (CardNotFoundException ignored) {

            }
        }

        if (printDebugMessages) {
            System.out.println("Ring-bearer resistance: " + resistance);
        }

        // Bid more if starting with Sam
        boolean startingWithSamSoh = false;
        try {
            startingWithSamSoh = StartingFellowshipUtil.getStartingFellowship(game.getGameState().getLotroDeck(aiPlayerName)).contains("1_311");
        } catch (UnsupportedOperationException ignored) {

        }

        Random random = new Random();
        int origin = startingWithSamSoh ? 3 : 0;
        int bound = (resistance / 2) + (startingWithSamSoh ? 2 : 0);
        int chosenNumber = random.nextInt(origin, bound);

        if (printDebugMessages) {
            System.out.println("Choosing randomly between " + origin + " (inclusive) and " + bound + " (exclusive)");
            if (startingWithSamSoh) {
                System.out.println("Bidding more because Sam is in starting fellowship");
            }
            System.out.println("Chosen: " + chosenNumber);
        }

        return chosenNumber;
    }

    public boolean wantToGoFirst(LotroGame game) {
        if (printDebugMessages) {
            printSeparator();
            System.out.println("Choosing whether to go first");
            System.out.println("Always go first");
        }
        return true;
    }

    public boolean wantToMulligan(LotroGame game) {
        boolean goingFirst = game.getGameState().getFirstPlayerId().equals(aiPlayerName);

        if (printDebugMessages) {
            printSeparator();
            System.out.println("Choosing whether to mulligan while " + (goingFirst ? "going first" : "going second"));
        }

        List<? extends PhysicalCard> hand = game.getGameState().getHand(aiPlayerName);

        if (printDebugMessages) {
            StringBuilder builder = new StringBuilder("Cards in hand: ");
            hand.forEach(card -> builder.append(card.getBlueprint().getFullName()).append("; "));
            System.out.println(builder);
        }

        if (hand.stream().anyMatch((Predicate<PhysicalCard>) physicalCard -> physicalCard.getBlueprintId().equals("8_20"))) {
            // Always keep hand with Saved from the Fire
            if (printDebugMessages) {
                System.out.println("Keeping hand with Saved from the Fire");
            }
            return false;
        }

        // TODO check if cards are playable
        int fpCards = Math.toIntExact(hand.stream().filter((Predicate<PhysicalCard>) physicalCard -> physicalCard.getBlueprint().getSide().equals(Side.FREE_PEOPLE)).count());
        int shadowCards = Math.toIntExact(hand.stream().filter((Predicate<PhysicalCard>) physicalCard -> physicalCard.getBlueprint().getSide().equals(Side.SHADOW)).count());

        // Can expect 3 cards after mulligan
        boolean atLeastThreeFpCards = fpCards >= 3;
        boolean atLeastThreeShadowCards = shadowCards >= 3;

        boolean keep = (goingFirst && atLeastThreeFpCards) || (!goingFirst && atLeastThreeShadowCards);

        if (printDebugMessages) {
            System.out.println((keep ? fpCards : shadowCards) +
                    (goingFirst ? " fp cards while going first" : " shadow cards while going second"));
            System.out.println(keep ? "Keep" : "Mulligan");

        }

        return !keep;
    }

    public boolean wantToMoveAgain(LotroGame game) {
        if (printDebugMessages) {
            printSeparator();
        }
        return movingAgainIsGood(game, aiPlayerName);
    }

    private boolean movingAgainIsGood(LotroGame game, String player) {
        int decision = 0;

        String opponentName = game.getGameState().getPlayerOrder().getAllPlayers().stream().filter(s -> !s.equals(player)).findFirst().orElseThrow();

        Set<PhysicalCard> minionsInPlay = game.getGameState().getInPlay().stream().filter((Predicate<PhysicalCard>) physicalCard -> physicalCard.getBlueprint().getCardType().equals(CardType.MINION)).collect(Collectors.toSet());
        Set<PhysicalCard> companionsInPlay = game.getGameState().getInPlay().stream().filter((Predicate<PhysicalCard>) physicalCard -> physicalCard.getBlueprint().getCardType().equals(CardType.COMPANION) && physicalCard.getOwner().equals(player)).collect(Collectors.toSet());
        int currentSite = game.getGameState().getPlayerPosition(player);
        int opponentSite = game.getGameState().getPlayerPosition(opponentName);

        // Remove balrogs from decision-making if moving from site 5 Fellowship block
        if (currentSite == 5 && game.getGameState().getCurrentSiteBlock().equals(SitesBlock.FELLOWSHIP)) {
            // Durin's Bane and Flame of Udun
            minionsInPlay.removeIf(physicalCard -> physicalCard.getBlueprintId().equals("2_51") || physicalCard.getBlueprintId().equals("2_52"));
        }

        int woundsOnCompanions = companionsInPlay.stream().mapToInt(value -> game.getGameState().getWounds(value)).sum();
        int companionsRiskingBeingKilled = Math.toIntExact(companionsInPlay.stream().filter(physicalCard -> game.getModifiersQuerying().getVitality(game, physicalCard) == 1 && !game.getGameState().getRingBearer(player).equals(physicalCard)).count());
        int dangerousMinions = Math.toIntExact(minionsInPlay.stream().filter(physicalCard -> physicalCard.getBlueprint().getTwilightCost() >= 5 || physicalCard.getBlueprint().getFullName().equals("Saruman, Keeper of Isengard")).count());

        if (printDebugMessages) {
            System.out.println("Determing if moving again is good for " + player);
            System.out.println("Current position: " + currentSite);
            System.out.println("Opponent position: " + opponentSite);
            System.out.println("Minions in play:");
            System.out.println(minionsInPlay.stream().map(physicalCard -> physicalCard.getBlueprint().getFullName()).toList());
            System.out.println("Wounds on companions: " + woundsOnCompanions);
            System.out.println("Companions with last vitality: " + companionsRiskingBeingKilled);
            System.out.println("Dangerous minion count: " + dangerousMinions);
        }

        // TODO play with numbers and test

        // No minions
        if (minionsInPlay.isEmpty()) {
            decision += 5;
            if (printDebugMessages) {
                System.out.println("No minions: +5");
            }
        }

        // Weak minions
        if (dangerousMinions == 0 && !minionsInPlay.isEmpty() && minionsInPlay.size() < 3 && minionsInPlay.stream().mapToInt(value -> value.getBlueprint().getTwilightCost()).sum() <= 5) {
            decision += 2;
            if (printDebugMessages) {
                System.out.println("Weak minions: +2");
            }
        }

        // Minions in play
        if (!minionsInPlay.isEmpty()) {
            decision -= minionsInPlay.size();
            if (printDebugMessages) {
                System.out.println("Minions in play: -" + minionsInPlay.size());
            }
        }

        // Sanctuary skip
        if ((currentSite == 3 || currentSite == 6) && woundsOnCompanions >= 3) {
            decision -= 5;
            if (printDebugMessages) {
                System.out.println("Skipping sanctuary with " + woundsOnCompanions + " wounds: -5");
            }
        }

        // Companion count
        int companionCountChange = (companionsInPlay.size() - 5) * 3;
        decision += companionCountChange;
        if (printDebugMessages) {
            System.out.println("Companion count: " + (companionCountChange >= 0 ? "+" : "") + companionCountChange);
        }

        // TODO damage +1 and fierce prediction
        // Last vitality non-RB companions
        if (companionsRiskingBeingKilled > 0) {
            decision -= companionsRiskingBeingKilled * 2;
            if (printDebugMessages) {
                System.out.println("Companions with last vitality: -" + companionsRiskingBeingKilled * 2);
            }
        }

        // Dangerous minions in play
        if (dangerousMinions > 0) {
            decision -= dangerousMinions;
            if (printDebugMessages) {
                System.out.println("Dangerous minions in play: -" + dangerousMinions);
            }
        }

        // Ring-bearer dying
        PhysicalCard ringbearer = game.getGameState().getRingBearer(player);
        if (game.getModifiersQuerying().getVitality(game, ringbearer) == 1
                && game.getModifiersQuerying().getResistance(game, ringbearer) == 1
                && companionsInPlay.stream().noneMatch(physicalCard -> physicalCard.getBlueprint().getTitle().equals("Sam")
                && !physicalCard.getBlueprint().getSubtitle().equals("Bearer of Great Need")
                && !physicalCard.getBlueprint().getSubtitle().equals("Steadfast Friend"))) {
            decision -= 5;
            if (printDebugMessages) {
                System.out.println("Ring-bearer dying: -5");
            }
        }

        // Low twilight
        if (game.getGameState().getTwilightPool() < 5) {
            decision += 3;
            if (printDebugMessages) {
                System.out.println("Low twilight: +1");
            }
        }

        // High twilight
        if (game.getGameState().getTwilightPool() > 8) {
            decision -= 3;
            if (printDebugMessages) {
                System.out.println("High twilight: -3");
            }
        }

        // Next site sanctuary
        if (currentSite == 2 || currentSite == 5) {
            decision += 1;
            if (printDebugMessages) {
                System.out.println("Move to sanctuary: +1");
            }
        }

        // Next site is ours
        if (game.getGameState().getInPlay().stream().anyMatch((Predicate<PhysicalCard>) physicalCard -> physicalCard.getBlueprint().getCardType().equals(CardType.SITE)
                && physicalCard.getBlueprint().getSiteNumber() == currentSite + 1
                && physicalCard.getOwner().equals(player))) {
            decision += 1;
            if (printDebugMessages) {
                System.out.println("Next site ours: +1");
            }
        }

        // FP events in hand
        // TODO cheating bot if it ends up too weak
        int fpEventsInHand = Math.toIntExact(game.getGameState().getHand(player).stream().filter((Predicate<PhysicalCard>) physicalCard -> physicalCard.getBlueprint().getSide().equals(Side.FREE_PEOPLE) && physicalCard.getBlueprint().getCardType().equals(CardType.EVENT)).count());
        if (fpEventsInHand > 0 && player.equals(aiPlayerName)) {
            decision += fpEventsInHand;
            if (printDebugMessages) {
                System.out.println("FP events in hand: +" + fpEventsInHand);
            }
        }

        // Behind on path
        if (currentSite <= opponentSite) {
            decision += 2;
            if (printDebugMessages) {
                System.out.println("Behind in race: +2");
            }
        }

        // Cannot win race
        int opponentMaxTurnsToNine = 9 - opponentSite;
        int minTurnsToNine = Math.ceilDiv(9 - (currentSite + 1), 2);
        if (opponentMaxTurnsToNine <= minTurnsToNine) {
            decision -= 100;
            if (printDebugMessages) {
                System.out.println("Too behind: -100");
            }
        }

        // Cannot lose race
        int maxTurnsToNine = 9 - currentSite;
        int opponentMinTurnsToNine = Math.ceilDiv(9 - currentSite, 2);
        if (maxTurnsToNine < opponentMinTurnsToNine) {
            decision -= 100;
            if (printDebugMessages) {
                System.out.println("Too ahead: -100");
            }
        }

        // Race finish
        if (currentSite == 8 && opponentSite >= 7) {
            decision += 2;
            if (printDebugMessages) {
                System.out.println("Pushing to site 9: +2");
            }
        }

        if (printDebugMessages) {
            System.out.println("Decision total: " + decision);
        }

        return decision > 0;

    }

    public String chooseNextStartingCompanionToPlay(LotroGame game) {
        if (printDebugMessages) {
            printSeparator();
            System.out.println("Choosing starting fellowship");
        }
        // Find complete starting fellowship
        List<String> startingFellowship = StartingFellowshipUtil.getStartingFellowship(game.getGameState().getLotroDeck(aiPlayerName));

        if (printDebugMessages) {
            if (BotService.staticLibrary != null) {
                List<String> names = startingFellowship.stream().map(s -> {
                    try {
                        return BotService.staticLibrary.getLotroCardBlueprint(s).getFullName();
                    } catch (CardNotFoundException ignored) {
                        return s;
                    }
                }).toList();
                System.out.println("Wanted companions: " + names);
            } else {
                System.out.println("Wanted companions: " + startingFellowship);
            }
        }

        List<? extends PhysicalCard> cardsInPlay = game.getGameState().getInPlay().stream().filter((Predicate<PhysicalCard>) physicalCard -> physicalCard.getOwner().equals(aiPlayerName)).toList();
        for (String companion : startingFellowship) {
            // Find the first one that has not been played already
            if (cardsInPlay.stream().anyMatch((Predicate<PhysicalCard>) physicalCard -> physicalCard.getBlueprintId().equals(companion))) {
                continue;
            }

            if (printDebugMessages) {
                if (BotService.staticLibrary != null) {
                    try {
                        System.out.println("Chosen: " + BotService.staticLibrary.getLotroCardBlueprint(companion).getFullName());
                    } catch (CardNotFoundException ignored) {
                        System.out.println("Chosen: " + companion);
                    }
                } else {
                    System.out.println("Chosen: " + companion);
                }
            }

            return companion;
        }

        if (printDebugMessages) {
            System.out.println("All companions played already, passing");
        }

        return ""; // Pass, all played already
    }

    public Map<String, List<String>> chooseAssignment(LotroGame game, AwaitingDecision awaitingDecision) {
        if (printDebugMessages) {
            printSeparator();
        }
        return new AiAssignmentController(aiPlayerName, printDebugMessages, game, awaitingDecision).chooseAssignment();
    }

    public PhysicalCard chooseOwnMinionToWound(LotroGame game, Collection<PhysicalCard> minions) {
        if (printDebugMessages) {
            printSeparator();
            StringBuilder shadow = new StringBuilder("Choosing own minion to wound");
            for (PhysicalCard minionCard : minions) {
                shadow.append("\n").append(minionCard.getBlueprint().getFullName()).append(" - vitality: ").append(game.getModifiersQuerying().getVitality(game, minionCard));
            }
            System.out.println(shadow);
        }

        PhysicalCard chosen = minions.stream()
                .max(Comparator.comparingInt((PhysicalCard c) -> game.getModifiersQuerying().getVitality(game, c))
                        .thenComparing(Comparator.comparingInt((PhysicalCard c) -> game.getModifiersQuerying().getStrength(game, c)).reversed()))
                .orElseThrow();


        if (printDebugMessages) {
            System.out.println("Chosen: " + chosen.getBlueprint().getFullName());
        }

        return chosen;
    }

    public PhysicalCard chooseOwnFpCharacterToWound(LotroGame game, Collection<PhysicalCard> fpCharacters) {
        if (printDebugMessages) {
            printSeparator();
            StringBuilder fp = new StringBuilder("Choosing own FP character to wound");
            for (PhysicalCard card : fpCharacters) {
                fp.append("\n").append(card.getBlueprint().getFullName()).append(" - vitality: ").append(game.getModifiersQuerying().getVitality(game, card));
            }
            System.out.println(fp);
        }

        // TODO prioritize ring bearer, count with burdens

        PhysicalCard chosen = fpCharacters.stream()
                .max((o1, o2) -> {
                    int vitality1 = game.getModifiersQuerying().getVitality(game, o1);
                    int vitality2 = game.getModifiersQuerying().getVitality(game, o2);

                    int strength1 = game.getModifiersQuerying().getStrength(game, o1);
                    int strength2 = game.getModifiersQuerying().getStrength(game, o2);

                    // Prioritize not killing
                    if (vitality1 == 1 && vitality2 != 1) {
                        return -1;
                    }
                    if (vitality1 != 1 && vitality2 == 1) {
                        return 1;
                    }

                    // Prioritize allies over companions
                    if (o1.getBlueprint().getCardType().equals(CardType.ALLY) && o2.getBlueprint().getCardType().equals(CardType.COMPANION)) {
                        return 1;
                    }
                    if (o2.getBlueprint().getCardType().equals(CardType.ALLY) && o1.getBlueprint().getCardType().equals(CardType.COMPANION)) {
                        return -1;
                    }

                    // Prioritize way weaker characters
                    if (strength1 > 2 * strength2) {
                        return -1;
                    }
                    if (strength2 > 2 * strength1) {
                        return 1;
                    }

                    // Prioritize healthier characters
                    if (vitality1 != vitality2) {
                        return Integer.compare(vitality1, vitality2);
                    }

                    // Prioritize weaker characters
                    return Integer.compare(strength2, strength1);
                })
                .orElseThrow();


        if (printDebugMessages) {
            System.out.println("Chosen: " + chosen.getBlueprint().getFullName());
        }

        return chosen;
    }

    public PhysicalCard chooseOwnFpCharacterToHeal(LotroGame game, Collection<PhysicalCard> fpCharacters) {
        if (printDebugMessages) {
            printSeparator();
            StringBuilder fp = new StringBuilder("Choosing own FP character to heal");
            for (PhysicalCard card : fpCharacters) {
                fp.append("\n").append(card.getBlueprint().getFullName()).append(" - vitality: ").append(game.getModifiersQuerying().getVitality(game, card));
            }
            System.out.println(fp);
        }

        PhysicalCard chosen = fpCharacters.stream()
                .min((o1, o2) -> {
                    int vitality1 = game.getModifiersQuerying().getVitality(game, o1);
                    int vitality2 = game.getModifiersQuerying().getVitality(game, o2);

                    int strength1 = game.getModifiersQuerying().getStrength(game, o1);
                    int strength2 = game.getModifiersQuerying().getStrength(game, o2);

                    if (vitality1 == 1 && vitality2 != 1) {
                        return -1;
                    }
                    if (vitality1 != 1 && vitality2 == 1) {
                        return 1;
                    }

                    if (o1.getBlueprint().getCardType().equals(CardType.ALLY) && o2.getBlueprint().getCardType().equals(CardType.COMPANION)) {
                        return 1;
                    }

                    if (o2.getBlueprint().getCardType().equals(CardType.ALLY) && o1.getBlueprint().getCardType().equals(CardType.COMPANION)) {
                        return -1;
                    }

                    if (strength1 > 2 * strength2) {
                        return -1;
                    }
                    if (strength2 > 2 * strength1) {
                        return 1;
                    }

                    if (vitality1 != vitality2) {
                        return Integer.compare(vitality1, vitality2);
                    }

                    return Integer.compare(strength2, strength1);
                })
                .orElseThrow();


        if (printDebugMessages) {
            System.out.println("Chosen: " + chosen.getBlueprint().getFullName());
        }

        return chosen;
    }

    public List<PhysicalCard> chooseCardsFromHandToDiscardToReconcile(LotroGame game, int min) {
        List<? extends PhysicalCard> cardsInHand = game.getGameState().getHand(aiPlayerName);
        if (printDebugMessages) {
            printSeparator();
            StringBuilder fp = new StringBuilder("Choosing card to discard from hand to reconcile");
            if (min > 0) {
                fp.append("\nNeed to discard ").append(min).append(" cards");
            }
            for (PhysicalCard card : cardsInHand) {
                fp.append("\n").append(card.getBlueprint().getFullName());
            }
            System.out.println(fp);
        }

//        // Check for unplayable cards in hand
//        boolean handContainsAlwaysDeadCard = cardsInHand.stream().anyMatch((Predicate<PhysicalCard>) card -> !BotCardFactory.create(card).canEverBePlayed());
//
//        if (handContainsAlwaysDeadCard) {
//            List<? extends PhysicalCard> toDiscard = cardsInHand.stream().filter((Predicate<PhysicalCard>) card -> !BotCardFactory.create(card).canEverBePlayed()).toList().subList(0, Math.max(1, min));
//            if (printDebugMessages) {
//                System.out.println("Discard unusable card: ");
//                for (PhysicalCard physicalCard : toDiscard) {
//                    System.out.println(physicalCard.getBlueprint().getFullName());
//                }
//            }
//            return (List<PhysicalCard>) toDiscard;
//        }

        // Not moving again, reconciling before playing shadow
        boolean fpReconcile = game.getGameState().getCurrentPlayerId().equals(aiPlayerName);
        // Reconciling in enemy turn, predict double move
        boolean canMoveAgain = !game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.CANT_MOVE)
                && game.getGameState().getMoveCount() < RuleUtils.calculateMoveLimit(game);
        boolean thinkOpponentWillMoveAgain = !fpReconcile && canMoveAgain && movingAgainIsGood(game, game.getGameState().getPlayerOrder().getAllPlayers().stream().filter(s -> !s.equals(aiPlayerName)).findFirst().orElseThrow());

        // Decide whether to discard FP or shadow card
        boolean wantToDiscardFpCard = fpReconcile || thinkOpponentWillMoveAgain;

        if (printDebugMessages) {
            if (fpReconcile) {
                System.out.println("Discarding before playing shadow card - want to discard FP card");
            } else if (thinkOpponentWillMoveAgain) {
                System.out.println("Discarding while expecting double move - want to discard FP card");
            } else {
                System.out.println("Discarding while expecting opponent to stay - want to discard shadow card");
            }
        }

        // Check if hand contains card of side bot wants to discard
        if (wantToDiscardFpCard && cardsInHand.stream().allMatch((Predicate<PhysicalCard>) card -> Side.SHADOW.equals(card.getBlueprint().getSide()))) {
            if (min == 0) {
                if (printDebugMessages) {
                    System.out.println("No FP cards in hand, not discarding");
                }
                return List.of();
            }
        } else if (!wantToDiscardFpCard && cardsInHand.stream().allMatch((Predicate<PhysicalCard>) card -> Side.FREE_PEOPLE.equals(card.getBlueprint().getSide()))) {
            if (min == 0) {
                if (printDebugMessages) {
                    System.out.println("No shadow cards in hand, not discarding");
                }
                return List.of();
            }
        }

        // Sort cards by side, immediate usefulness, and then card type
        // TODO count with some card value
        List<PhysicalCard> toDiscard = (List<PhysicalCard>) cardsInHand.stream()
                .sorted(
                        Comparator
                                .comparingInt((ToIntFunction<PhysicalCard>) card ->
                                        card.getBlueprint().getSide().equals(wantToDiscardFpCard ? Side.FREE_PEOPLE : Side.SHADOW) ? 0 : 1
                                )
                                .thenComparingInt(card -> switch (card.getBlueprint().getCardType()) {
                                    case THE_ONE_RING, ADVENTURE, MAP, SITE -> 0;
                                    case EVENT -> 1;
                                    case FOLLOWER -> 2;
                                    case CONDITION -> 3 + (card.getBlueprint().getSide().equals(Side.SHADOW) ? 4 : 0);
                                    case ALLY -> 4;
                                    case POSSESSION -> 5;
                                    case MINION -> 6;
                                    case ARTIFACT -> 7;
                                    case COMPANION -> 8;
                                })
                )
                .limit(Math.max(1, min))
                .toList();

        if (printDebugMessages) {
            if (toDiscard.isEmpty()) {
                System.out.println("No suitable card to discard");
            } else {
                if (toDiscard.size() > 1) {
                    System.out.println("Discarding " + toDiscard.size() + " cards");
                }
                for (PhysicalCard physicalCard : toDiscard) {
                    System.out.println("Discard: " + physicalCard.getBlueprint().getFullName());
                    System.out.println("Correct side: " + physicalCard.getBlueprint().getSide().equals(wantToDiscardFpCard ? Side.FREE_PEOPLE : Side.SHADOW));
                }
            }
        }

        return toDiscard;
    }

    public PhysicalCard chooseNextSkirmishToResolve(LotroGame game, List<PhysicalCard> options) {
        if (printDebugMessages) {
            printSeparator();
        }

        return new AiSkirmishOrderController(printDebugMessages, game, options).chooseNextSkirmish();
    }

    public List<PhysicalCard> chooseTargetForEffect(LotroGame game, List<PhysicalCard> options, PhysicalCard source, AwaitingDecision awaitingDecision) {
        if (printDebugMessages) {
            printSeparator();
        }

        if (fellowshipPhasePlan != null && !fellowshipPhasePlan.replanningNeeded()) {
            List<PhysicalCard> plannedTargets = fellowshipPhasePlan.chooseTarget(awaitingDecision);
            int min = Integer.parseInt(awaitingDecision.getDecisionParameters().get("min")[0]);
            int max = Integer.parseInt(awaitingDecision.getDecisionParameters().get("max")[0]);
            if (options.containsAll(plannedTargets) && min <= plannedTargets.size() && max >= plannedTargets.size()) {
                return plannedTargets;
            }
        } else if (betweenTurnsPlan != null && !betweenTurnsPlan.replanningNeeded()) {
            List<PhysicalCard> plannedTargets = betweenTurnsPlan.chooseTarget(awaitingDecision);
            int min = Integer.parseInt(awaitingDecision.getDecisionParameters().get("min")[0]);
            int max = Integer.parseInt(awaitingDecision.getDecisionParameters().get("max")[0]);
            if (options.containsAll(plannedTargets) && min <= plannedTargets.size() && max >= plannedTargets.size()) {
                return plannedTargets;
            }
        } else if (shadowPhasePlan != null && !shadowPhasePlan.replanningNeeded()) {
            // TODO remove when shadow phase plan is finished
            try {
                List<PhysicalCard> plannedTargets = shadowPhasePlan.chooseTarget(awaitingDecision);
                int min = Integer.parseInt(awaitingDecision.getDecisionParameters().get("min")[0]);
                int max = Integer.parseInt(awaitingDecision.getDecisionParameters().get("max")[0]);
                if (options.containsAll(plannedTargets) && min <= plannedTargets.size() && max >= plannedTargets.size()) {
                    return plannedTargets;
                }
            } catch (Exception e) {
                throw new UnsupportedOperationException("Shadow plan target error: " + e.getMessage());
            }
        }

        return new AiTargetController(printDebugMessages, game, options, source, awaitingDecision).chooseTarget();
    }

    public String chooseOptionForEffect(LotroGame game, AwaitingDecision awaitingDecision) throws CardNotFoundException {
        if (printDebugMessages) {
            printSeparator();
        }

        return new AiMultipleChoiceController(printDebugMessages, game, awaitingDecision).chooseOption();
    }

    public int chooseRequiredResponseToResolveNext(LotroGame game, AwaitingDecision awaitingDecision) throws CardNotFoundException {
        if (printDebugMessages) {
            printSeparator();
        }

        return new AiRequiredResponseOrderController(printDebugMessages, game, awaitingDecision).chooseOption();
    }

    public int chooseIntegerForEffect(LotroGame game, AwaitingDecision awaitingDecision) throws CardNotFoundException {
        if (printDebugMessages) {
            printSeparator();
        }

        return new AiIntegerChoiceController(printDebugMessages, game, awaitingDecision).chooseNumber();
    }

    public int chooseActionToTakeNext(LotroGame game, AwaitingDecision awaitingDecision) throws CardNotFoundException {
        if (printDebugMessages) {
            printSeparator();
        }

        if (awaitingDecision.getText().equals("Play Fellowship action or Pass")) {
            if (fellowshipPhasePlan == null || fellowshipPhasePlan.replanningNeeded()) {
                fellowshipPhasePlan = new FellowshipPhasePlan(printDebugMessages, game);
            }
            try {
                return fellowshipPhasePlan.chooseActionToTakeOrPass(awaitingDecision);
            } catch (Exception e) {
                throw new UnsupportedOperationException("Fellowship plan error: " + e.getMessage());
            }
        } else if (!game.getGameState().getCurrentPlayerId().equals(aiPlayerName) && game.getGameState().getCurrentPhase().equals(Phase.SHADOW)) {
            if (shadowPhasePlan == null || shadowPhasePlan.replanningNeeded()) {
                shadowPhasePlan = new ShadowPhasePlan(printDebugMessages, game);
            }
            try {
                return shadowPhasePlan.chooseActionToTakeOrPass(awaitingDecision);
            } catch (Exception e) {
                throw new UnsupportedOperationException("Shadow plan error: " + e.getMessage());
            }
        } else if (game.getGameState().getCurrentPhase().equals(Phase.BETWEEN_TURNS)) {
            if (betweenTurnsPlan == null || betweenTurnsPlan.replanningNeeded()) {
                betweenTurnsPlan = new BetweenTurnsPlan(printDebugMessages, game);
            }
            return betweenTurnsPlan.chooseActionToTakeOrPass(awaitingDecision);
        } else {
            throw new UnsupportedOperationException("Decision not supported: " + awaitingDecision.toJson().toString());
        }
    }
}
