package com.gempukku.lotro.bots.forge.plan;

import com.gempukku.lotro.bots.forge.plan.action.*;
import com.gempukku.lotro.bots.forge.cards.ability2.TriggeredAbility;
import com.gempukku.lotro.bots.forge.cards.ability2.cost.Cost;
import com.gempukku.lotro.bots.forge.cards.ability2.cost.CostWithTarget;
import com.gempukku.lotro.bots.forge.cards.ability2.effect.EffectWithTarget;
import com.gempukku.lotro.bots.forge.cards.abstractcard.BotCard;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;

import java.util.*;
import java.util.stream.Collectors;

public class BetweenTurnsPlan {
    private final int siteNumber;
    private final String playerName;
    private final LotroGame game;
    private final boolean printDebugMessages;

    ActionToTake lastAction = null;
    private final PlannedBoardState plannedBoardState;

    public BetweenTurnsPlan(boolean printDebugMessages, LotroGame game) {
        this.siteNumber = game.getGameState().getCurrentSiteNumber();
        this.playerName = game.getGameState().getCurrentPlayerId();
        this.game = game;
        this.printDebugMessages = printDebugMessages;

        if (printDebugMessages) {
            System.out.println("New between turns phase plan - player " + playerName + " is about to play at site " + siteNumber);
        }

        plannedBoardState = new PlannedBoardState(game, playerName);
    }

    public boolean replanningNeeded() {
        return !isActive();
    }

    private boolean isActive() {
        boolean tbr =  game.getGameState().getCurrentPlayerId().equals(playerName)
                && game.getGameState().getCurrentPhase().equals(Phase.BETWEEN_TURNS)
                && game.getGameState().getCurrentSiteNumber() == siteNumber;
        if (printDebugMessages) {
            System.out.println("Plan is active: " + tbr);
        }
        return tbr;
    }

    public int chooseActionToTakeOrPass(AwaitingDecision awaitingDecision) {
        List<BotCard> cards = Arrays.stream(awaitingDecision.getDecisionParameters().get("cardId")).map(Integer::parseInt).map(plannedBoardState::getCardById).toList();
        Map<BotCard, Integer> cardsWithPositions = new HashMap<>();
        for (int i = 0; i < cards.size(); i++) {
            cardsWithPositions.put(cards.get(i), i);
        }

        if (cards.stream().allMatch(botCard -> botCard.getTriggeredAbility() == null)) {
            throw new UnsupportedOperationException("Decision not supported: " + awaitingDecision.toJson().toString());
        }

        double maxValue = 0.0;
        BotCard chosenCard = null;

        for (BotCard botCard : cards) {
            TriggeredAbility triggeredAbility = botCard.getTriggeredAbility();
            if (triggeredAbility == null) {
                continue;
            }
            double value = triggeredAbility.getPossibleValue(playerName, plannedBoardState);
            if (value > maxValue) {
                maxValue = value;
                chosenCard = botCard;
            }
        }


        if (chosenCard == null) {
            String joined = cards.stream()
                    .map(t -> t.getSelf().getBlueprint().getFullName())
                    .collect(Collectors.joining("; "));
            if (printDebugMessages) {
                System.out.println("Will pass without using any triggered ability among: " + joined);
            }
            return -1;
        } else {
            UseCardWithTargetAction.Targeting effectTargeting = null;
            UseCardWithTargetAction.Targeting costTargeting = null;
            if (EffectWithTarget.class.isAssignableFrom(chosenCard.getTriggeredAbility().getEffect().getClass())) {
                List<BotCard> targets = new ArrayList<>();
                List<BotCard> potentialTargets = ((EffectWithTarget) chosenCard.getTriggeredAbility().getEffect()).getPotentialTargets(playerName, plannedBoardState);
                if (((EffectWithTarget) chosenCard.getTriggeredAbility().getEffect()).affectsAll() || potentialTargets.size() <= 1) {
                    targets.addAll(potentialTargets);
                } else {
                    targets.add(((EffectWithTarget) chosenCard.getTriggeredAbility().getEffect()).chooseTarget(playerName, plannedBoardState));
                }

                if (printDebugMessages) {
                    System.out.println("Will use triggered ability of " + chosenCard.getSelf().getBlueprint().getFullName() + " to " + ((EffectWithTarget) chosenCard.getTriggeredAbility().getEffect()).toString(playerName, plannedBoardState, targets));
                }
                if (targets.isEmpty()) {

                } else if (targets.size() == 1) {
                    effectTargeting = new UseCardWithTargetAction.Targeting(targets.getFirst(), potentialTargets);
                } else {
                    throw new IllegalStateException("Cannot resolve activated ability effect if number of targets greater than 1");
                }
            } else {
                if (printDebugMessages) {
                    System.out.println("Will use triggered ability of " + chosenCard.getSelf().getBlueprint().getFullName() + " to " + chosenCard.getTriggeredAbility().getEffect().toString(playerName, plannedBoardState));
                }
            }

            Cost cost = chosenCard.getTriggeredAbility().getCost();
            if (cost instanceof CostWithTarget costWithTarget) {
                List<BotCard> potentialTargets = costWithTarget.getPotentialTargets(playerName, plannedBoardState);
                BotCard target = costWithTarget.chooseTarget(playerName, plannedBoardState);
                costTargeting = new UseCardWithTargetAction.Targeting(target, potentialTargets);
            }

            List<UseCardWithTargetAction.Targeting> targetings = new ArrayList<>();
            if (effectTargeting != null) {
                targetings.add(effectTargeting);
            }
            if (costTargeting != null) {
                targetings.add(costTargeting);
            }

            if (cost != null) {
                if (costTargeting != null) {
                    System.out.println("Cost to pay: " + ((CostWithTarget) cost).toString(playerName, plannedBoardState, costTargeting.target()));
                } else {
                    System.out.println("Cost to pay: " + cost.toString(playerName, plannedBoardState));
                }
            }

            if (effectTargeting == null && costTargeting == null) {
                lastAction = new UseCardAction(chosenCard, chosenCard.getTriggeredAbility().getEffect().getClass());
            } else {
                lastAction = new UseCardWithTargetAction(
                        chosenCard,
                        targetings,
                        chosenCard.getTriggeredAbility().getEffect().getClass());
            }

            if (effectTargeting != null && costTargeting != null) {
                plannedBoardState.activateTriggeredAbilityOnTargetWithCostTarget(chosenCard, playerName, List.of(effectTargeting.target()), costTargeting.target());
            } else if (effectTargeting != null) {
                plannedBoardState.activateTriggeredAbilityOnTarget(chosenCard, playerName, List.of(effectTargeting.target()));
            } else if (costTargeting != null) {
                plannedBoardState.activateTriggeredAbilityWithCostTarget(chosenCard, playerName, costTargeting.target());
            } else {
                plannedBoardState.activateTriggeredAbility(chosenCard, playerName);
            }

            return cardsWithPositions.get(chosenCard);
        }
    }

    public List<PhysicalCard> chooseTarget(AwaitingDecision awaitingDecision) {
        if (printDebugMessages) {
            System.out.println("Between turns plan asked to take action on " + awaitingDecision.toJson().toString());
        }

        if (!isActive()) {
            if (printDebugMessages) {
                System.out.println("Plan is outdated");
            }
            throw new IllegalStateException("Plan is outdated");
        }

        if (lastAction == null) {
            if (printDebugMessages) {
                System.out.println("No action taken yet, cannot choose target");
            }
            throw new IllegalStateException("No action taken yet, cannot choose target");
        }

        if (printDebugMessages) {
            System.out.println("Last action");
            System.out.println(lastAction);
        }

        if (lastAction instanceof UseCardWithTargetAction actionWithTarget) {
            List<Integer> physicalIds = new ArrayList<>();
            if (awaitingDecision.getDecisionType().equals(AwaitingDecisionType.ARBITRARY_CARDS)) {
                for (String physicalCard : awaitingDecision.getDecisionParameters().get("physicalId")) {
                    physicalIds.add(Integer.parseInt(physicalCard));
                }

            } else if (awaitingDecision.getDecisionType().equals(AwaitingDecisionType.CARD_SELECTION)) {
                for (String physicalCard : awaitingDecision.getDecisionParameters().get("cardId")) {
                    physicalIds.add(Integer.parseInt(physicalCard));
                }
            }
            if (printDebugMessages) {
                System.out.println("Target chosen by plan: " + actionWithTarget.getTarget(physicalIds).getBlueprint().getFullName());
            }
            return List.of(actionWithTarget.getTarget(physicalIds));
        } else {
            throw new IllegalStateException("Last action should not trigger targeting");
        }
    }
}
