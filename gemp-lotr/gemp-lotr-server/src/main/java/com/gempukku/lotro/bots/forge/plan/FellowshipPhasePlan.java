package com.gempukku.lotro.bots.forge.plan;

import com.gempukku.lotro.bots.forge.plan.action.ActionToTake;
import com.gempukku.lotro.bots.forge.plan.action.ChooseTargetsAction;
import com.gempukku.lotro.bots.forge.plan.action.PassAction;
import com.gempukku.lotro.bots.forge.utils.DecisionToActions;
import com.gempukku.lotro.bots.forge.utils.TargetFinderUtil;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.DefaultUserFeedback;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.*;

import static com.gempukku.lotro.bots.forge.utils.BotLogging.log;

public class FellowshipPhasePlan implements Plan {
    private final int siteNumber;
    private final String playerName;
    private final DefaultLotroGame game;

    private int nextStep = 0;
    List<ActionToTake> actions2 = new ArrayList<>();

    private final DefaultLotroGame copy;
    private final DefaultUserFeedback feedback = new DefaultUserFeedback();

    public FellowshipPhasePlan(DefaultLotroGame game) {
        this.siteNumber = game.getGameState().getCurrentSiteNumber();
        this.playerName = game.getGameState().getCurrentPlayerId();
        this.game = game;

        log(1, "Making new fellowship phase plan for site " + siteNumber, true);

        copy = game.getCopyByReplayingDecisionsFromStart(feedback);

        makePlan();
    }

    private void makePlan() {
        while (true) {
            AwaitingDecision awaitingDecision = feedback.getAwaitingDecision(playerName);
            log(2, "Decision expected: " + awaitingDecision.toJson());
            List<ActionToTake> decisionActions = DecisionToActions.toActions(awaitingDecision, copy);
            log(2, "Possible actions: ");
            for (ActionToTake decisionAction : decisionActions) {
                log(2, decisionAction.toString());
            }
            ActionToTake chosenAction2 = chooseAction(decisionActions);

            actions2.add(chosenAction2);
            log(1, "  " + actions2.size() + ". " + chosenAction2);

            String answer = chosenAction2.carryOut();
            feedback.participantDecided(playerName, answer);
            try {
                awaitingDecision.decisionMade(answer);
            } catch (DecisionResultInvalidException e) {
                throw new IllegalStateException("Chosen action was invalid: " + answer, e);
            }
            copy.carryOutPendingActionsUntilDecisionNeeded();

            if (chosenAction2 instanceof PassAction && awaitingDecision.getText().equals("Play Fellowship action or Pass")) {
                break;
            }
        }
    }



    private ActionToTake chooseAction(List<ActionToTake> possibleActions) {
        if (possibleActions.stream().allMatch(actionToTake -> actionToTake instanceof ChooseTargetsAction)) {
            return TargetFinderUtil.getBestTarget(possibleActions.stream().map(actionToTake -> ((ChooseTargetsAction) actionToTake)).toList(), copy, playerName);
        }
        //TODO
        return possibleActions.getFirst();
    }

//    private ActionToTake chooseAction(List<ActionToTake> possibleActions) {
//        if (possibleActions.stream().allMatch(actionToTake -> actionToTake instanceof ChooseTargetsAction2)) {
//            return TargetFinderUtil.getBestTarget(possibleActions.stream().map(actionToTake -> ((ChooseTargetsAction2) actionToTake)).toList(), copy, playerName);
//        }
//
//        // Priority order of effects to consider
//        List<Class<? extends Effect>> effectPriority = List.of(
//                EffectRevealOpponentsHand.class,
//                EffectDiscardFromPlay.class,
//                EffectRemoveBurden.class,
//                EffectPlayFellowshipsNextSite.class,
//                EffectHeal.class,
//                EffectTakeIntoHandFromDiscard.class,
//                EffectPlayWithBonus.class
//        );
//
//        for (Class<? extends Effect> effectClass : effectPriority) {
//            ActionToTake bestAction = getBestActionWithEffect(possibleActions, effectClass);
//            if (bestAction != null) {
//                return bestAction;
//            }
//        }
//
//        // Check for heal companion by discard actions
//        Optional<ActionToTake> healAction = possibleActions.stream()
//                .filter(actionToTake -> actionToTake instanceof DiscardCompanionToHealAction2)
//                .findFirst();
//        if (healAction.isPresent()) {
//            return healAction.get();
//        }
//
//        // Play permanents from hand
//        ActionToTake bestPlayPermanentAction = getBestPlayPermanentFromHandAction(possibleActions);
//        if (bestPlayPermanentAction != null) {
//            return bestPlayPermanentAction;
//        }
//
//        // Priority order of effects to unclog hand
//        List<Class<? extends Effect>> unclogEffectPriority = List.of(
//                EffectPutFromHandToBottomOfDeck.class
//        );
//
//        for (Class<? extends Effect> effectClass : unclogEffectPriority) {
//            ActionToTake bestAction = getBestActionWithEffect(possibleActions, effectClass);
//            if (bestAction != null) {
//                return bestAction;
//            }
//        }
//
//        // Finally pass if no other action with value is found
//        return possibleActions.stream()
//                .filter(actionToTake -> actionToTake instanceof PassAction2)
//                .findFirst()
//                .orElseThrow(() -> new IllegalStateException("No valid action found, and no pass action available."));
//
//    }
//
//    private ActionToTake getBestPlayPermanentFromHandAction(List<ActionToTake> possibleActions) {
//        List<ActionToTake> playPermanentActions = new ArrayList<>(possibleActions.stream()
//                .filter(action -> action instanceof PlayCardFromHandAction2 playCardFromHandAction
//                        && !(playCardFromHandAction.getCard() instanceof BotEventCard))
//                .sorted(playPermanentComparator())
//                .toList());
//
//        if (playPermanentActions.isEmpty()) {
//            return null;
//        }
//        return playPermanentActions.getFirst();
//    }
//
//    private ActionToTake getBestActionWithEffect(List<ActionToTake> possibleActions, Class<? extends Effect> effectClass) {
//        List<ActionToTake> actionsWithEffect = new ArrayList<>(possibleActions.stream().filter(valuableCardsWithEffect(effectClass)).sorted(eventAbilityComparator(effectClass)).toList());
//        if (actionsWithEffect.isEmpty()) {
//            return null;
//        }
//        return actionsWithEffect.getFirst();
//    }
//
//    private Comparator<ActionToTake> eventAbilityComparator(Class<? extends Effect> effectClass) {
//        // Sort actions: events first, then abilities, each group sorted by value (highest first)
//        return (action1, action2) -> {
//            boolean isEvent1 = action1 instanceof PlayCardFromHandAction2;
//            boolean isEvent2 = action2 instanceof PlayCardFromHandAction2;
//
//            // Events come before abilities
//            if (isEvent1 && !isEvent2) return -1;
//            if (!isEvent1 && isEvent2) return 1;
//
//            // Both are same type, sort by value (highest first)
//            double value1 = getActionValue(action1, effectClass);
//            double value2 = getActionValue(action2, effectClass);
//            return Double.compare(value2, value1); // Reversed for descending order
//        };
//    }
//
//    private Comparator<ActionToTake> playPermanentComparator() {
//        return (action1, action2) -> {
//            if (!(action1 instanceof PlayCardFromHandAction2 play1) || !(action2 instanceof PlayCardFromHandAction2 play2)) {
//                return 0;
//            }
//
//            CardType type1 = play1.getCard().getSelf().getBlueprint().getCardType();
//            CardType type2 = play2.getCard().getSelf().getBlueprint().getCardType();
//
//            // Define priority order: companion (0), ally (1), possession (2), condition (3)
//            int priority1 = getCardTypePriority(type1);
//            int priority2 = getCardTypePriority(type2);
//
//            if (priority1 != priority2) {
//                return Integer.compare(priority1, priority2);
//            }
//
//            // If both are companions, sort by strength (highest first)
//            if (type1 == CardType.COMPANION && type2 == CardType.COMPANION) {
//                int strength1 = play1.getCard().getSelf().getBlueprint().getStrength();
//                int strength2 = play2.getCard().getSelf().getBlueprint().getStrength();
//                return Integer.compare(strength2, strength1); // Reversed for descending order
//            }
//
//            return 0;
//        };
//    }
//
//    private int getCardTypePriority(CardType cardType) {
//        return switch (cardType) {
//            case COMPANION -> 0;
//            case ALLY -> 1;
//            case POSSESSION -> 2;
//            case CONDITION -> 3;
//            default -> 4;
//        };
//    }
//
//    private double getActionValue(ActionToTake action, Class<? extends Effect> effectClass) {
//        if (action instanceof PlayCardFromHandAction2 playCardFromHandAction) {
//            if (playCardFromHandAction.getCard() instanceof BotEventCard botEventCard
//                    && botEventCard.getEventAbility().getEffect().getClass().equals(effectClass)) {
//                return botEventCard.getEventAbility().getPossibleValue(playerName, copy);
//            }
//        } else if (action instanceof UseCardAction2 useCardAction) {
//            BotCard botCard = useCardAction.getCard();
//            ActivatedAbility activatedAbility = botCard.getActivatedAbility(effectClass);
//            return activatedAbility.getPossibleValue(playerName, copy);
//        }
//        return 0.0;
//    }
//
//    private Predicate<ActionToTake> valuableCardsWithEffect(Class<? extends Effect> effectClass) {
//        return action -> {
//            if (action instanceof PlayCardFromHandAction2 playCardFromHandAction) {
//                if (playCardFromHandAction.getCard() instanceof BotEventCard botEventCard) {
//                    if (botEventCard.getEventAbility().getEffect().getClass().equals(effectClass)) {
//                        double value = botEventCard.getEventAbility().getPossibleValue(playerName, copy);
//                        return value >= 0.0; // play cards with 0 value to cycle hand
//                    }
//                }
//            } else if (action instanceof UseCardAction2 useCardAction) {
//                BotCard botCard = useCardAction.getCard();
//                ActivatedAbility activatedAbility = botCard.getActivatedAbility(effectClass);
//                if (activatedAbility != null) {
//                    double value = activatedAbility.getPossibleValue(playerName, copy);
//                    return value > 0.0;
//                }
//            }
//            return false;
//        };
//    }

    @Override
    public String chooseActionToTakeOrPass(DefaultLotroGame game, AwaitingDecision awaitingDecision) {
        log(1, "Fellowship phase plan asked to take action on " + awaitingDecision.toJson().toString(), true);

        if (!isActive()) {
            log(1, "Fellowship plan is outdated");
            throw new IllegalStateException("Plan is outdated");
        }

        if (nextStep >= actions2.size()) {
            log(1, "All actions from plan already taken");
            throw new IllegalStateException("All actions from plan already taken");
        }

        ActionToTake action = actions2.get(nextStep);
        log(1, "Action " + (nextStep + 1) + " out of " + actions2.size() + ": "+ action.toString());
        nextStep++;
        return action.carryOut();
    }

    @Override
    public boolean isOutdated() {
        return !isActive() || nextStep >= actions2.size();
    }

    private boolean isActive() {
        return game.getGameState().getCurrentPlayerId().equals(playerName)
                && game.getGameState().getCurrentPhase().equals(Phase.FELLOWSHIP)
                && game.getGameState().getCurrentSiteNumber() == siteNumber;
    }

    @Override
    public void decisionMadeByPlayer(AwaitingDecision awaitingDecision, String answer, String player) {
        // During fellowship phase the player plays without interruptions, opponent makes no decisions
    }
}
