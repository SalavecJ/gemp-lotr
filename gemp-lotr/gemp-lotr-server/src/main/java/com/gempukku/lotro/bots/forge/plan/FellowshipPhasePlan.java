package com.gempukku.lotro.bots.forge.plan;

import com.gempukku.lotro.bots.forge.plan.action.*;
import com.gempukku.lotro.bots.forge.cards.BotTargetingMode;
import com.gempukku.lotro.bots.forge.cards.ability2.ActivatedAbility;
import com.gempukku.lotro.bots.forge.cards.ability2.cost.CostWithTarget;
import com.gempukku.lotro.bots.forge.cards.ability2.effect.*;
import com.gempukku.lotro.bots.forge.cards.abstractcard.*;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class FellowshipPhasePlan {
    private final int siteNumber;
    private final String playerName;
    private final LotroGame game;
    private final boolean printDebugMessages;

    private int nextStep = 0;
    List<ActionToTake> actions = new ArrayList<>();
    private final PlannedBoardState plannedBoardState;

    public FellowshipPhasePlan(boolean printDebugMessages, LotroGame game) {
        this.siteNumber = game.getGameState().getCurrentSiteNumber();
        this.playerName = game.getGameState().getCurrentPlayerId();
        this.game = game;
        this.printDebugMessages = printDebugMessages;

        if (printDebugMessages) {
            System.out.println("Making new fellowship phase plan for site " + siteNumber);
        }

        plannedBoardState = new PlannedBoardState(game, playerName);
//        makePlan();
        makePlan2();
    }

    private void makePlan2() {
        while (true) {
            List<ActionToTake> possibleActions = plannedBoardState.getAvailableActions(playerName);
            ActionToTake action = chooseAction(possibleActions);
            actions.add(action);
            if (printDebugMessages) {
                System.out.println("  " + actions.size() + ". " + action);
            }
            plannedBoardState.takeAction(playerName, action);
            if (action instanceof PassAction) {
                break;
            }
        }
    }

    private ActionToTake chooseAction(List<ActionToTake> possibleActions) {
        // Mandatory target choosing actions for attached cards
        if (possibleActions.stream().allMatch(actionToTake -> actionToTake instanceof ChooseTargetForAttachmentAction)) {
            return getBestTargetForAttachment(possibleActions);
        }
        // Mandatory target choosing actions for cost
        if (possibleActions.stream().allMatch(actionToTake -> actionToTake instanceof ChooseTargetForCostAction)) {
            return getBestTargetForCost(possibleActions);
        }
        // Mandatory target choosing actions for effect
        if (possibleActions.stream().allMatch(actionToTake -> actionToTake instanceof ChooseTargetForEffectAction)) {
            return getBestTargetForEffect(possibleActions);
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
            ActionToTake bestAction = getBestActionWithEffect(possibleActions, effectClass);
            if (bestAction != null) {
                return bestAction;
            }
        }

        // Check for heal companion by discard actions
        Optional<ActionToTake> healAction = possibleActions.stream()
                .filter(actionToTake -> actionToTake instanceof DiscardCompanionToHealAction)
                .findFirst();
        if (healAction.isPresent()) {
            return healAction.get();
        }

        // Play permanents from hand
        ActionToTake bestPlayPermanentAction = getBestPlayPermanentFromHandAction(possibleActions);
        if (bestPlayPermanentAction != null) {
            return bestPlayPermanentAction;
        }

        // Priority order of effects to unclog hand
        List<Class<? extends Effect>> unclogEffectPriority = List.of(
                EffectPutFromHandToBottomOfDeck.class
        );

        for (Class<? extends Effect> effectClass : unclogEffectPriority) {
            ActionToTake bestAction = getBestActionWithEffect(possibleActions, effectClass);
            if (bestAction != null) {
                return bestAction;
            }
        }

        // Finally pass if no other action with value is found
        return possibleActions.stream()
                .filter(actionToTake -> actionToTake instanceof PassAction)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No valid action found, and no pass action available."));
    }

    private ActionToTake getBestTargetForAttachment(List<ActionToTake> possibleActions) {
        ChooseTargetForAttachmentAction firstAction = validateAndGetFirstAction(
                possibleActions,
                ChooseTargetForAttachmentAction.class,
                "ChooseTargetForAttachmentAction"
        );

        BotCard attachment = firstAction.getAttachment();
        verifyAllActionsShareSameCard(possibleActions,
                action -> ((ChooseTargetForAttachmentAction) action).getAttachment(),
                "attachment");

        if (!(attachment instanceof BotObjectAttachableCard attachableCard)) {
            throw new IllegalStateException("Attachment is not a BotObjectAttachableCard: " + attachment.getSelf().getBlueprint().getFullName());
        }

        List<BotCard> potentialTargets = possibleActions.stream()
                .map(action -> ((ChooseTargetForAttachmentAction) action).getTarget())
                .toList();

        BotTargetingMode attachTargetingMode = attachableCard.getAttachTargetingMode();
        BotCard chosenTarget = attachTargetingMode.chooseTarget(plannedBoardState, potentialTargets, false);

        return findActionWithTarget(possibleActions, chosenTarget,
                action -> ((ChooseTargetForAttachmentAction) action).getTarget(),
                attachment.getSelf().getBlueprint().getFullName());
    }

    private ActionToTake getBestTargetForCost(List<ActionToTake> possibleActions) {
        ChooseTargetForCostAction firstAction = validateAndGetFirstAction(
                possibleActions,
                ChooseTargetForCostAction.class,
                "ChooseTargetForCostAction"
        );

        BotCard source = firstAction.getSource();
        CostWithTarget cost = firstAction.getCost();

        verifyAllActionsShareSameCard(possibleActions,
                action -> ((ChooseTargetForCostAction) action).getSource(),
                "source");

        BotCard chosenTarget = cost.chooseTarget(playerName, plannedBoardState);

        return findActionWithTarget(possibleActions, chosenTarget,
                action -> ((ChooseTargetForCostAction) action).getTarget(),
                source.getSelf().getBlueprint().getFullName());
    }

    private ActionToTake getBestTargetForEffect(List<ActionToTake> possibleActions) {
        ChooseTargetForEffectAction firstAction = validateAndGetFirstAction(
                possibleActions,
                ChooseTargetForEffectAction.class,
                "ChooseTargetForEffectAction"
        );

        BotCard source = firstAction.getSource();
        EffectWithTarget effect = firstAction.getEffect();

        verifyAllActionsShareSameCard(possibleActions,
                action -> ((ChooseTargetForEffectAction) action).getSource(),
                "source");

        BotCard chosenTarget = effect.chooseTarget(playerName, plannedBoardState);

        return findActionWithTarget(possibleActions, chosenTarget,
                action -> ((ChooseTargetForEffectAction) action).getTarget(),
                source.getSelf().getBlueprint().getFullName());
    }

    private ActionToTake getBestPlayPermanentFromHandAction(List<ActionToTake> possibleActions) {
        List<ActionToTake> playPermanentActions = new ArrayList<>(possibleActions.stream()
                .filter(action -> action instanceof PlayCardFromHandAction playCardFromHandAction
                        && !(playCardFromHandAction.getCard() instanceof BotEventCard))
                .sorted(playPermanentComparator())
                .toList());

        if (playPermanentActions.isEmpty()) {
            return null;
        }
        return playPermanentActions.getFirst();
    }

    private ActionToTake getBestActionWithEffect(List<ActionToTake> possibleActions, Class<? extends Effect> effectClass) {
        List<ActionToTake> actionsWithEffect = new ArrayList<>(possibleActions.stream().filter(valuableCardsWithEffect(effectClass)).sorted(eventAbilityComparator(effectClass)).toList());
        if (actionsWithEffect.isEmpty()) {
            return null;
        }
        return actionsWithEffect.getFirst();
    }

    private Comparator<ActionToTake> eventAbilityComparator(Class<? extends Effect> effectClass) {
        // Sort actions: events first, then abilities, each group sorted by value (highest first)
        return (action1, action2) -> {
            boolean isEvent1 = action1 instanceof PlayCardFromHandAction;
            boolean isEvent2 = action2 instanceof PlayCardFromHandAction;

            // Events come before abilities
            if (isEvent1 && !isEvent2) return -1;
            if (!isEvent1 && isEvent2) return 1;

            // Both are same type, sort by value (highest first)
            double value1 = getActionValue(action1, effectClass);
            double value2 = getActionValue(action2, effectClass);
            return Double.compare(value2, value1); // Reversed for descending order
        };
    }

    private Comparator<ActionToTake> playPermanentComparator() {
        return (action1, action2) -> {
            if (!(action1 instanceof PlayCardFromHandAction play1) || !(action2 instanceof PlayCardFromHandAction play2)) {
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

    private double getActionValue(ActionToTake action, Class<? extends Effect> effectClass) {
        if (action instanceof PlayCardFromHandAction playCardFromHandAction) {
            if (playCardFromHandAction.getCard() instanceof BotEventCard botEventCard
                    && botEventCard.getEventAbility().getEffect().getClass().equals(effectClass)) {
                return botEventCard.getEventAbility().getPossibleValue(playerName, plannedBoardState);
            }
        } else if (action instanceof UseCardAction useCardAction) {
            BotCard botCard = useCardAction.getCard();
            ActivatedAbility activatedAbility = botCard.getActivatedAbility(effectClass);
            return activatedAbility.getPossibleValue(playerName, plannedBoardState);
        }
        return 0.0;
    }

    private Predicate<ActionToTake> valuableCardsWithEffect(Class<? extends Effect> effectClass) {
        return action -> {
            if (action instanceof PlayCardFromHandAction playCardFromHandAction) {
                if (playCardFromHandAction.getCard() instanceof BotEventCard botEventCard) {
                    if (botEventCard.getEventAbility().getEffect().getClass().equals(effectClass)) {
                        double value = botEventCard.getEventAbility().getPossibleValue(playerName, plannedBoardState);
                        return value >= 0.0; // play cards with 0 value to cycle hand
                    }
                }
            } else if (action instanceof UseCardAction useCardAction) {
                BotCard botCard = useCardAction.getCard();
                ActivatedAbility activatedAbility = botCard.getActivatedAbility(effectClass);
                if (activatedAbility != null) {
                    double value = activatedAbility.getPossibleValue(playerName, plannedBoardState);
                    return value > 0.0;
                }
            }
            return false;
        };
    }

    // Helper methods for target choosing refactoring

    private <T extends ActionToTake> T validateAndGetFirstAction(
            List<ActionToTake> possibleActions,
            Class<T> expectedClass,
            String expectedClassName) {
        if (possibleActions.stream().anyMatch(action -> !expectedClass.isInstance(action))) {
            throw new IllegalStateException("Expected all actions to be " + expectedClassName);
        }
        return expectedClass.cast(possibleActions.getFirst());
    }

    private void verifyAllActionsShareSameCard(
            List<ActionToTake> possibleActions,
            Function<ActionToTake, BotCard> cardExtractor,
            String cardDescription) {
        BotCard firstCard = cardExtractor.apply(possibleActions.getFirst());
        for (ActionToTake action : possibleActions) {
            BotCard card = cardExtractor.apply(action);
            if (!card.equals(firstCard)) {
                throw new IllegalStateException("Not all actions share the same " + cardDescription);
            }
        }
    }

    private ActionToTake findActionWithTarget(
            List<ActionToTake> possibleActions,
            BotCard chosenTarget,
            Function<ActionToTake, BotCard> targetExtractor,
            String sourceName) {
        if (chosenTarget == null) {
            throw new IllegalStateException("Could not find target for " + sourceName);
        }

        return possibleActions.stream()
                .filter(action -> targetExtractor.apply(action).equals(chosenTarget))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Could not find action for chosen target"));
    }

    public int chooseActionToTakeOrPass(AwaitingDecision awaitingDecision) {
        if (printDebugMessages) {
            System.out.println("Fellowship phase plan asked to take action on " + awaitingDecision.toJson().toString());
        }

        if (!isActive()) {
            if (printDebugMessages) {
                System.out.println("Plan is outdated");
            }
            throw new IllegalStateException("Plan is outdated");
        }

        if (nextStep >= actions.size()) {
            System.out.println("All actions from plan already taken");
            throw new IllegalStateException("All actions from plan already taken");
        }

        ActionToTake action = actions.get(nextStep);
        if (printDebugMessages) {
            System.out.println("Action " + (nextStep + 1) + " out of " + actions.size());
            System.out.println(action.toString());
        }
        int result = action.carryOut(awaitingDecision);
        nextStep++;
        return result;
    }

    public List<PhysicalCard> chooseTarget(AwaitingDecision awaitingDecision) {
        if (printDebugMessages) {
            System.out.println("Fellowship phase plan asked to take action on " + awaitingDecision.toJson().toString());
        }

        if (!isActive()) {
            if (printDebugMessages) {
                System.out.println("Plan is outdated");
            }
            throw new IllegalStateException("Plan is outdated");
        }

        if (nextStep >= actions.size()) {
            System.out.println("All actions from plan already taken");
            throw new IllegalStateException("All actions from plan already taken");
        }

        ActionToTake action = actions.get(nextStep);
        if (!(action instanceof ChooseTargetAction)) {
            throw new IllegalStateException("Next action in plan is not target action");
        }
        if (printDebugMessages) {
            System.out.println("Action " + (nextStep + 1) + " out of " + actions.size());
            System.out.println(action.toString());
        }
        nextStep++;
        return List.of(((ChooseTargetAction) action).getTarget().getSelf());
    }

    public boolean replanningNeeded() {
        return !isActive() || nextStep >= actions.size();
    }

    private boolean isActive() {
        return game.getGameState().getCurrentPlayerId().equals(playerName)
                && game.getGameState().getCurrentPhase().equals(Phase.FELLOWSHIP)
                && game.getGameState().getCurrentSiteNumber() == siteNumber;
    }
}
