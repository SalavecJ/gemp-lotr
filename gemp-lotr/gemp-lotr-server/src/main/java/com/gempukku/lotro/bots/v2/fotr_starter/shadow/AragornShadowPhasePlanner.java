package com.gempukku.lotro.bots.v2.fotr_starter.shadow;

import com.gempukku.lotro.bots.v2.decision.option.ActivateCardOption;
import com.gempukku.lotro.bots.v2.decision.option.Option;
import com.gempukku.lotro.bots.v2.decision.option.PassOption;
import com.gempukku.lotro.bots.v2.decision.option.PlayCardOption;
import com.gempukku.lotro.bots.v2.decision.request.DecisionRequest;
import com.gempukku.lotro.bots.v2.fotr_starter.Plan;
import com.gempukku.lotro.bots.v2.fotr_starter.SimplePlan;
import com.gempukku.lotro.bots.v2.simulation.SimulatedGame;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.*;
import java.util.function.Function;

import static com.gempukku.lotro.bots.v2.fotr_starter.utility.CardPredicates.*;
import static com.gempukku.lotro.bots.v2.fotr_starter.utility.GameQueries.*;
import static com.gempukku.lotro.bots.v2.fotr_starter.utility.OptionFinders.*;

public class AragornShadowPhasePlanner {
    private static final String SARUMANS_AMBITION = "Saruman's Ambition";
    private static final String URUKHAI_ARMORY = "Uruk-hai Armory";
    private static final String THEIR_ARROWS_ENRAGE = "Their Arrows Enrage";
    private static final String URUK_SLAYER = "Uruk Slayer";
    private static final int SWAPPABLE_URUK_TWILIGHT_COST = 4;

    public static Plan getPlan(DefaultLotroGame game, String player) {
        // Try playing as many minions as possible, the simulation ends up in the state after the swarm plan
        SimulatedGame swarmSimulation = new SimulatedGame(game);
        Plan swarmPlan = getSwarmPlan(game, swarmSimulation, player);

        // Swarm is worth it only if we can outnumber the opponent's companions
        if (getNumberOfMinionsInPlay(swarmSimulation, player) > getNumberOfCompanionsInPlay(swarmSimulation, game.getGameState().getOpponent(player))) {
            return swarmPlan;
        }

        // If we can't outnumber, try to play the strongest minions
        return getPlayStrongestMinionsPlan(game, player);
    }

    private static Plan getSwarmPlan(DefaultLotroGame game, SimulatedGame swarmSimulation, String player) {
        Comparator<PhysicalCard> cheapestFirst = Comparator.comparingInt((PhysicalCard card) -> card.getBlueprint().getTwilightCost()).reversed();
        // Try to play as many minions as possible, starting with the cheapest ones
        SimulatedGame basicSimulation = new SimulatedGame(game);
        Plan basicPlan = runShadowPhase(basicSimulation, player,
                decision -> chooseMinionOrAcceptResponse(decision, cheapestFirst));

        int extraTwilight = basicSimulation.getGame().getGameState().getTwilightPool();

        List<PhysicalCard> playedMinions = getPlayedCards(basicPlan);
        List<PhysicalCard> minionsInHand = getMinionsInHand(basicSimulation, player);

        // Swap the weakest minions in play for stronger ones in hand, while there is enough twilight to do so
        // The total number of minions played will not change, but the strength of the minions in play will potentially increase at the cost of spending more twilight
        Map<PhysicalCard, PhysicalCard> minionsToSwap = getMinionsToSwapInSwarm(playedMinions, minionsInHand, extraTwilight);

        List<PhysicalCard> minionsToPlay = new ArrayList<>(playedMinions);
        minionsToPlay.removeAll(minionsToSwap.keySet());
        minionsToPlay.addAll(0, minionsToSwap.values());

        // Play the minions, use remaining twilight to play conditions, if any
        return runShadowPhase(swarmSimulation, player,
                decision -> chooseReplayOrFallback(decision, minionsToPlay, true));
    }

    private static Map<PhysicalCard, PhysicalCard> getMinionsToSwapInSwarm(List<PhysicalCard> playedMinions, List<PhysicalCard> minionsInHand, int extraTwilight) {
        if (playedMinions.isEmpty() || minionsInHand.isEmpty()) {
            return Collections.emptyMap();
        }

        List<PhysicalCard> minionsInPlay = new ArrayList<>(playedMinions);
        List<PhysicalCard> minionsInHandCopy = new ArrayList<>(minionsInHand);
        int twilightToSpend = extraTwilight;

        while (true) {
            PhysicalCard weakestMinionInPlay = minionsInPlay.stream()
                    .min(Comparator.comparingInt(c -> c.getBlueprint().getStrength()))
                    .orElse(null);
            if (weakestMinionInPlay == null) {
                throw new IllegalStateException("No minions in play to swap");
            }

            PhysicalCard cheapestStrongerMinionInHand = minionsInHandCopy.stream()
                    .filter(c -> c.getBlueprint().getStrength() > weakestMinionInPlay.getBlueprint().getStrength())
                    .min(Comparator.comparingInt(c -> c.getBlueprint().getTwilightCost()))
                    .orElse(null);
            if (cheapestStrongerMinionInHand == null) {
                // No stronger minion in hand to swap with, we are done
                break;
            }

            int twilightCostDifference = cheapestStrongerMinionInHand.getBlueprint().getTwilightCost() - weakestMinionInPlay.getBlueprint().getTwilightCost();
            if (twilightCostDifference > twilightToSpend) {
                // Not enough twilight to swap, we are done
                break;
            }

            // Perform the swap
            minionsInPlay.remove(weakestMinionInPlay);
            minionsInPlay.add(cheapestStrongerMinionInHand);
            minionsInHandCopy.remove(cheapestStrongerMinionInHand);
            twilightToSpend -= twilightCostDifference;
        }

        return buildSwapMap(playedMinions, minionsInPlay);
    }

    private static Plan getPlayStrongestMinionsPlan(DefaultLotroGame game, String player) {
        Comparator<PhysicalCard> strongestFirst = Comparator.comparingInt(card -> card.getBlueprint().getStrength());
        // Try to play as many minions as possible, starting with the strongest ones
        Plan basicPlan = runShadowPhase(new SimulatedGame(game), player,
                decision -> chooseMinionOrAcceptResponse(decision, strongestFirst));
        List<PhysicalCard> playedMinions = getPlayedCards(basicPlan);

        // Use remaining twilight to play conditions, if any
        SimulatedGame withConditionsSimulation = new SimulatedGame(game);
        Plan withConditionsPlan = runShadowPhase(withConditionsSimulation, player,
                decision -> chooseReplayOrFallback(decision, playedMinions, true));

        int extraTwilight = withConditionsSimulation.getGame().getGameState().getTwilightPool();

        List<PhysicalCard> playedCards = getPlayedCards(withConditionsPlan);
        List<PhysicalCard> minionsInHand = getMinionsInHand(withConditionsSimulation, player);

        // If after playing conditions we have extra twilight, it is preferred to have Uruk Slayer in play, as it can use the twilight later
        // Extra twilight + Uruk Slayer in hand = play Uruk Slayer instead of some other 4-cost minion in play
        // No twilight + Uruk Slayer in play = swap Uruk Slayer for some other 4-cost minion in hand, if any
        Map<PhysicalCard, PhysicalCard> minionsToSwap = getMinionsToSwapForUrukSlayer(playedCards, minionsInHand, extraTwilight);
        List<PhysicalCard> cardsToPlay = new ArrayList<>(playedCards);
        cardsToPlay.removeAll(minionsToSwap.keySet());
        cardsToPlay.addAll(0, minionsToSwap.values());

        return runShadowPhase(new SimulatedGame(game), player,
                decision -> chooseReplayOrFallback(decision, cardsToPlay, false));
    }

    private static Map<PhysicalCard, PhysicalCard> getMinionsToSwapForUrukSlayer(List<PhysicalCard> playedCards, List<PhysicalCard> minionsInHand, int extraTwilight) {
        List<PhysicalCard> cardsInPlay = new ArrayList<>(playedCards);
        List<PhysicalCard> urukSlayersInPlay = new ArrayList<>(cardsInPlay.stream()
                .filter(hasFullName(URUK_SLAYER))
                .toList());
        List<PhysicalCard> swappableUruksInPlay = new ArrayList<>(cardsInPlay.stream()
                .filter(AragornShadowPhasePlanner::isSwappableUruk)
                .toList());
        List<PhysicalCard> urukSlayersInHand = new ArrayList<>(minionsInHand.stream()
                .filter(hasFullName(URUK_SLAYER))
                .toList());
        List<PhysicalCard> swappableUruksInHand = new ArrayList<>(minionsInHand.stream()
                .filter(AragornShadowPhasePlanner::isSwappableUruk)
                .toList());

        boolean preferToPlayUrukSlayer = extraTwilight > 0;

        if (preferToPlayUrukSlayer) {
            while (!urukSlayersInHand.isEmpty() && !swappableUruksInPlay.isEmpty()) {
                PhysicalCard urukSlayerToPlay = urukSlayersInHand.removeFirst();
                PhysicalCard urukToSwapOut = swappableUruksInPlay.removeFirst();

                cardsInPlay.remove(urukToSwapOut);
                cardsInPlay.add(urukSlayerToPlay);
            }
        } else {
            while (!urukSlayersInPlay.isEmpty() && !swappableUruksInHand.isEmpty()) {
                PhysicalCard urukSlayerToSwapOut = urukSlayersInPlay.removeFirst();
                PhysicalCard urukToSwapIn = swappableUruksInHand.removeFirst();

                cardsInPlay.remove(urukSlayerToSwapOut);
                cardsInPlay.add(urukToSwapIn);
            }
        }

        return buildSwapMap(playedCards, cardsInPlay);
    }

    private static boolean isSwappableUruk(PhysicalCard card) {
        return hasCardType(CardType.MINION).test(card)
                && card.getBlueprint().getTwilightCost() == SWAPPABLE_URUK_TWILIGHT_COST
                && !card.getBlueprint().getFullName().equals(URUK_SLAYER);
    }

    // Plays the shadow phase in the simulation, asking the chooser for an option on each decision
    private static Plan runShadowPhase(SimulatedGame simulation, String player, Function<DecisionRequest, Option> chooser) {
        List<List<Option>> actionsToTake = new ArrayList<>();
        DecisionRequest nextDecision = simulation.getDecisionRequest(player);

        while (nextDecision != null && simulation.getGame().getGameState().getCurrentPhase() == Phase.SHADOW) {
            Option optionToTake = chooser.apply(nextDecision);
            actionsToTake.add(List.of(optionToTake));
            simulation.makeDecision(player, optionToTake.getAnswerForGame());
            nextDecision = simulation.getDecisionRequest(player);
        }

        return new SimplePlan(actionsToTake);
    }

    // Chooser for the runShadowPhase method
    private static Option chooseMinionOrAcceptResponse(DecisionRequest decision, Comparator<PhysicalCard> minionPreference) {
        if (decision.isShadowPhaseDecision()) {
            // Play the most preferred minion according to the comparator, pass if no minion can be played
            return choosePlayMinionAction(decision, minionPreference);
        } else if (decision.isOptionalResponsesDecision()) {
            // Accept the trigger of Uruk Soldier
            return chooseAcceptOptionalResponse(decision);
        } else {
            throw new IllegalStateException("Unexpected decision type: " + decision);
        }
    }

    private static Option choosePlayMinionAction(DecisionRequest decision, Comparator<PhysicalCard> preference) {
        List<Option> options = decision.getOptions();

        Option passOption = findOptionOfType(options, PassOption.class);
        if (passOption == null) {
            throw new IllegalStateException("No pass option found in decision: " + decision);
        }

        // Find the most preferred minion to play, if any, otherwise pass
        return options.stream()
                .filter(option -> option instanceof PlayCardOption)
                .map(option -> (PlayCardOption) option)
                .filter(playCardOption -> hasCardType(CardType.MINION).test(playCardOption.getCard()))
                .max(Comparator.comparing(PlayCardOption::getCard, preference))
                .map(option -> (Option) option)
                .orElse(passOption);
    }

    // Chooser for the runShadowPhase method, replays the queued cards in order (consuming the queue), then optionally plays conditions.
    // Cards are matched by full name and not by card id: ids change when a card moves from hand to play, so an id
    // Of a card played in the simulation cannot be compared with the id of the card in hand in the original game.
    // Copies of the same card in hand are interchangeable, so whichever copy is found first is played.
    private static Option chooseReplayOrFallback(DecisionRequest decision, List<PhysicalCard> cardsToPlay, boolean playConditions) {
        if (decision.isOptionalResponsesDecision()) {
            // Accept the trigger of Uruk Soldier
            return chooseAcceptOptionalResponse(decision);
        } else if (!cardsToPlay.isEmpty()) {
            // Play the next card in the queue
            PhysicalCard cardToPlay = cardsToPlay.removeFirst();
            return findPlay(decision.getOptions(), hasFullName(cardToPlay.getBlueprint().getFullName()));
        } else if (playConditions) {
            // If requested, play conditions in order of preference, pass if none can be played
            return choosePlayConditionOrPass(decision.getOptions());
        } else {
            return findOptionOfType(decision.getOptions(), PassOption.class);
        }
    }

    private static Option chooseAcceptOptionalResponse(DecisionRequest nextDecision) {
        List<Option> options = nextDecision.getOptions();

        // Accept the trigger of Uruk Soldier
        Option acceptOption = findOptionOfType(options, ActivateCardOption.class);
        if (acceptOption == null) {
            throw new IllegalStateException("No accept option found in decision: " + nextDecision);
        }
        return acceptOption;
    }

    private static Option choosePlayConditionOrPass(List<Option> options) {
        List<String> conditionPriority = List.of(SARUMANS_AMBITION, URUKHAI_ARMORY, THEIR_ARROWS_ENRAGE);
        for (String condition : conditionPriority) {
            Option option = findPlay(options, hasFullName(condition));
            if (option != null) {
                return option;
            }
        }
        return findOptionOfType(options, PassOption.class);
    }

    private static List<PhysicalCard> getPlayedCards(Plan plan) {
        List<PhysicalCard> playedCards = new ArrayList<>();
        for (List<Option> action : plan.getAllOptions()) {
            if (action.size() != 1) {
                throw new IllegalStateException("Unexpected action size: " + action);
            }
            if (action.getFirst() instanceof PlayCardOption playCardOption) {
                playedCards.add(playCardOption.getCard());
            }
        }
        return playedCards;
    }

    private static List<PhysicalCard> getMinionsInHand(SimulatedGame simulatedGame, String player) {
        return simulatedGame.getGame().getGameState().getHand(player).stream()
                .filter(hasCardType(CardType.MINION))
                .map(c -> ((PhysicalCard) c))
                .toList();
    }

    private static Map<PhysicalCard, PhysicalCard> buildSwapMap(List<PhysicalCard> originalCards, List<PhysicalCard> cardsAfterSwap) {
        List<PhysicalCard> swappedOut = new ArrayList<>(originalCards);
        swappedOut.removeAll(cardsAfterSwap);

        List<PhysicalCard> swappedIn = new ArrayList<>(cardsAfterSwap);
        swappedIn.removeAll(originalCards);

        if (swappedOut.size() != swappedIn.size()) {
            throw new IllegalStateException("Mismatch in swapped minions: " + swappedOut + " vs " + swappedIn);
        }

        Map<PhysicalCard, PhysicalCard> swapMap = new HashMap<>();
        for (int i = 0; i < swappedOut.size(); i++) {
            swapMap.put(swappedOut.get(i), swappedIn.get(i));
        }
        return swapMap;
    }
}