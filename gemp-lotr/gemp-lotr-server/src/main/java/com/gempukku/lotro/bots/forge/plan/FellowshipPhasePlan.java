package com.gempukku.lotro.bots.forge.plan;

import com.gempukku.lotro.bots.forge.cards.ability2.ActivatedAbility;
import com.gempukku.lotro.bots.forge.cards.ability2.effect.*;
import com.gempukku.lotro.bots.forge.cards.abstractcard.*;
import com.gempukku.lotro.bots.forge.plan.action2.*;
import com.gempukku.lotro.bots.forge.utils.DecisionToActions;
import com.gempukku.lotro.bots.forge.utils.TargetFinderUtil;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.DefaultUserFeedback;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.*;
import java.util.function.Predicate;

public class FellowshipPhasePlan {
    private final int siteNumber;
    private final String playerName;
    private final DefaultLotroGame game;
    private final boolean printDebugMessages;

    private int nextStep = 0;
    List<ActionToTake2> actions2 = new ArrayList<>();

    private final DefaultLotroGame copy;
    private final DefaultUserFeedback feedback = new DefaultUserFeedback();

    public FellowshipPhasePlan(boolean printDebugMessages, DefaultLotroGame game) {
        this.siteNumber = game.getGameState().getCurrentSiteNumber();
        this.playerName = game.getGameState().getCurrentPlayerId();
        this.game = game;
        this.printDebugMessages = printDebugMessages;

        if (printDebugMessages) {
            System.out.println("Making new fellowship phase plan for site " + siteNumber);
        }

        copy = game.getCopyByReplayingDecisionsFromStart(feedback);

        makePlan();
    }

    private void makePlan() {
        while (true) {
            AwaitingDecision awaitingDecision = feedback.getAwaitingDecision(playerName);
//            System.out.println("Decision expected: " + awaitingDecision.toJson());
            List<ActionToTake2> decisionActions = DecisionToActions.toActions(awaitingDecision, copy);
//            System.out.println("Possible actions: ");
//            for (ActionToTake2 decisionAction : decisionActions) {
//                System.out.println(decisionAction);
//            }
            ActionToTake2 chosenAction2 = chooseAction(decisionActions);

            actions2.add(chosenAction2);
            if (printDebugMessages) {
                System.out.println("  " + actions2.size() + ". " + chosenAction2);
            }

            String answer = chosenAction2.carryOut();
            feedback.participantDecided(playerName, answer);
            try {
                awaitingDecision.decisionMade(answer);
            } catch (DecisionResultInvalidException e) {
                throw new IllegalStateException("Chosen action was invalid: " + answer, e);
            }
            copy.carryOutPendingActionsUntilDecisionNeeded();

            if (chosenAction2 instanceof PassAction2 && awaitingDecision.getText().equals("Play Fellowship action or Pass")) {
                break;
            }
        }
    }

    private ActionToTake2 chooseAction(List<ActionToTake2> possibleActions) {
        if (possibleActions.stream().allMatch(actionToTake2 -> actionToTake2 instanceof ChooseTargetsAction2)) {
            return TargetFinderUtil.getBestTarget(possibleActions.stream().map(actionToTake2 -> ((ChooseTargetsAction2) actionToTake2)).toList(), copy, playerName);
        }

        // Priority order of effects to consider
        List<Class<? extends Effect>> effectPriority = List.of(
                EffectRevealOpponentsHand.class,
                EffectDiscardFromPlay.class,
                EffectRemoveBurden.class,
                EffectPlayFellowshipsNextSite.class,
                EffectHeal.class,
                EffectTakeIntoHandFromDiscard.class,
                EffectPlayWithBonus.class
        );

        for (Class<? extends Effect> effectClass : effectPriority) {
            ActionToTake2 bestAction = getBestActionWithEffect(possibleActions, effectClass);
            if (bestAction != null) {
                return bestAction;
            }
        }

        // Check for heal companion by discard actions
        Optional<ActionToTake2> healAction = possibleActions.stream()
                .filter(actionToTake -> actionToTake instanceof DiscardCompanionToHealAction2)
                .findFirst();
        if (healAction.isPresent()) {
            return healAction.get();
        }

        // Play permanents from hand
        ActionToTake2 bestPlayPermanentAction = getBestPlayPermanentFromHandAction(possibleActions);
        if (bestPlayPermanentAction != null) {
            return bestPlayPermanentAction;
        }

        // Priority order of effects to unclog hand
        List<Class<? extends Effect>> unclogEffectPriority = List.of(
                EffectPutFromHandToBottomOfDeck.class
        );

        for (Class<? extends Effect> effectClass : unclogEffectPriority) {
            ActionToTake2 bestAction = getBestActionWithEffect(possibleActions, effectClass);
            if (bestAction != null) {
                return bestAction;
            }
        }

        // Finally pass if no other action with value is found
        return possibleActions.stream()
                .filter(actionToTake -> actionToTake instanceof PassAction2)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No valid action found, and no pass action available."));

    }

    private ActionToTake2 getBestPlayPermanentFromHandAction(List<ActionToTake2> possibleActions) {
        List<ActionToTake2> playPermanentActions = new ArrayList<>(possibleActions.stream()
                .filter(action -> action instanceof PlayCardFromHandAction2 playCardFromHandAction
                        && !(playCardFromHandAction.getCard() instanceof BotEventCard))
                .sorted(playPermanentComparator())
                .toList());

        if (playPermanentActions.isEmpty()) {
            return null;
        }
        return playPermanentActions.getFirst();
    }

    private ActionToTake2 getBestActionWithEffect(List<ActionToTake2> possibleActions, Class<? extends Effect> effectClass) {
        List<ActionToTake2> actionsWithEffect = new ArrayList<>(possibleActions.stream().filter(valuableCardsWithEffect(effectClass)).sorted(eventAbilityComparator(effectClass)).toList());
        if (actionsWithEffect.isEmpty()) {
            return null;
        }
        return actionsWithEffect.getFirst();
    }

    private Comparator<ActionToTake2> eventAbilityComparator(Class<? extends Effect> effectClass) {
        // Sort actions: events first, then abilities, each group sorted by value (highest first)
        return (action1, action2) -> {
            boolean isEvent1 = action1 instanceof PlayCardFromHandAction2;
            boolean isEvent2 = action2 instanceof PlayCardFromHandAction2;

            // Events come before abilities
            if (isEvent1 && !isEvent2) return -1;
            if (!isEvent1 && isEvent2) return 1;

            // Both are same type, sort by value (highest first)
            double value1 = getActionValue(action1, effectClass);
            double value2 = getActionValue(action2, effectClass);
            return Double.compare(value2, value1); // Reversed for descending order
        };
    }

    private Comparator<ActionToTake2> playPermanentComparator() {
        return (action1, action2) -> {
            if (!(action1 instanceof PlayCardFromHandAction2 play1) || !(action2 instanceof PlayCardFromHandAction2 play2)) {
                return 0;
            }

            CardType type1 = play1.getCard().getSelf().getBlueprint().getCardType();
            CardType type2 = play2.getCard().getSelf().getBlueprint().getCardType();

            // Define priority order: companion (0), ally (1), possession (2), condition (3)
            int priority1 = getCardTypePriority(type1);
            int priority2 = getCardTypePriority(type2);

            if (priority1 != priority2) {
                return Integer.compare(priority1, priority2);
            }

            // If both are companions, sort by strength (highest first)
            if (type1 == CardType.COMPANION && type2 == CardType.COMPANION) {
                int strength1 = play1.getCard().getSelf().getBlueprint().getStrength();
                int strength2 = play2.getCard().getSelf().getBlueprint().getStrength();
                return Integer.compare(strength2, strength1); // Reversed for descending order
            }

            return 0;
        };
    }

    private int getCardTypePriority(CardType cardType) {
        return switch (cardType) {
            case COMPANION -> 0;
            case ALLY -> 1;
            case POSSESSION -> 2;
            case CONDITION -> 3;
            default -> 4;
        };
    }

    private double getActionValue(ActionToTake2 action, Class<? extends Effect> effectClass) {
        if (action instanceof PlayCardFromHandAction2 playCardFromHandAction) {
            if (playCardFromHandAction.getCard() instanceof BotEventCard botEventCard
                    && botEventCard.getEventAbility().getEffect().getClass().equals(effectClass)) {
                return botEventCard.getEventAbility().getPossibleValue(playerName, copy);
            }
        } else if (action instanceof UseCardAction2 useCardAction) {
            BotCard botCard = useCardAction.getCard();
            ActivatedAbility activatedAbility = botCard.getActivatedAbility(effectClass);
            return activatedAbility.getPossibleValue(playerName, copy);
        }
        return 0.0;
    }

    private Predicate<ActionToTake2> valuableCardsWithEffect(Class<? extends Effect> effectClass) {
        return action -> {
            if (action instanceof PlayCardFromHandAction2 playCardFromHandAction) {
                if (playCardFromHandAction.getCard() instanceof BotEventCard botEventCard) {
                    if (botEventCard.getEventAbility().getEffect().getClass().equals(effectClass)) {
                        double value = botEventCard.getEventAbility().getPossibleValue(playerName, copy);
                        return value >= 0.0; // play cards with 0 value to cycle hand
                    }
                }
            } else if (action instanceof UseCardAction2 useCardAction) {
                BotCard botCard = useCardAction.getCard();
                ActivatedAbility activatedAbility = botCard.getActivatedAbility(effectClass);
                if (activatedAbility != null) {
                    double value = activatedAbility.getPossibleValue(playerName, copy);
                    return value > 0.0;
                }
            }
            return false;
        };
    }

    public String chooseActionToTakeOrPass(AwaitingDecision awaitingDecision) {
        if (printDebugMessages) {
            System.out.println("Fellowship phase plan asked to take action on " + awaitingDecision.toJson().toString());
        }

        if (!isActive()) {
            if (printDebugMessages) {
                System.out.println("Plan is outdated");
            }
            throw new IllegalStateException("Plan is outdated");
        }

        if (nextStep >= actions2.size()) {
            System.out.println("All actions from plan already taken");
            throw new IllegalStateException("All actions from plan already taken");
        }

        ActionToTake2 action = actions2.get(nextStep);
        if (printDebugMessages) {
            System.out.println("Action " + (nextStep + 1) + " out of " + actions2.size());
            System.out.println(action.toString());
        }
        String result = action.carryOut();
        nextStep++;
        return result;
    }

    public String chooseTarget(AwaitingDecision awaitingDecision) {
        if (printDebugMessages) {
            System.out.println("Fellowship phase plan asked to take action on " + awaitingDecision.toJson().toString());
        }

        if (!isActive()) {
            if (printDebugMessages) {
                System.out.println("Plan is outdated");
            }
            throw new IllegalStateException("Plan is outdated");
        }

        if (nextStep >= actions2.size()) {
            System.out.println("All actions from plan already taken");
            throw new IllegalStateException("All actions from plan already taken");
        }

        ActionToTake2 action = actions2.get(nextStep);
        if (!(action instanceof ChooseTargetsAction2)) {
            throw new IllegalStateException("Next action in plan is not target action");
        }
        if (printDebugMessages) {
            System.out.println("Action " + (nextStep + 1) + " out of " + actions2.size());
            System.out.println(action);
        }
        nextStep++;
        return action.carryOut();
    }

    public boolean replanningNeeded() {
        return !isActive() || nextStep >= actions2.size();
    }

    private boolean isActive() {
        return game.getGameState().getCurrentPlayerId().equals(playerName)
                && game.getGameState().getCurrentPhase().equals(Phase.FELLOWSHIP)
                && game.getGameState().getCurrentSiteNumber() == siteNumber;
    }
}
