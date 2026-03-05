package com.gempukku.lotro.bots.forge.plan;

import com.gempukku.lotro.bots.forge.cards.abstractcards.BotCard;
import com.gempukku.lotro.bots.forge.cards.abstractcards.BotEventCard;
import com.gempukku.lotro.bots.forge.plan.action.*;
import com.gempukku.lotro.bots.forge.utils.DecisionToActions;
import com.gempukku.lotro.bots.forge.utils.TargetFinderUtil;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.DefaultUserFeedback;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.*;
import java.util.function.BiPredicate;

import static com.gempukku.lotro.bots.forge.utils.BotLogging.log;

public class FellowshipPhasePlan implements Plan {
    private final int siteNumber;
    private final String playerName;
    private final DefaultLotroGame game;

    private int nextStep = 0;
    List<ActionToTake> actions = new ArrayList<>();

    private final DefaultLotroGame copy;
    private final DefaultUserFeedback feedback = new DefaultUserFeedback();

    private boolean opponentsHandRevealed = false;
    private BotCard cardToPlayFromHand = null;

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
            ActionToTake chosenAction = chooseAction(decisionActions);

            actions.add(chosenAction);
            log(1, "  " + actions.size() + ". " + chosenAction);

            String answer = chosenAction.carryOut();
            feedback.participantDecided(playerName, answer);
            try {
                awaitingDecision.decisionMade(answer);
            } catch (DecisionResultInvalidException e) {
                throw new IllegalStateException("Chosen action was invalid: " + answer, e);
            }
            copy.carryOutPendingActionsUntilDecisionNeeded();

            if (chosenAction instanceof PassAction && awaitingDecision.getText().equals("Play Fellowship action or Pass")) {
                break;
            }
        }
    }



    private ActionToTake chooseAction(List<ActionToTake> possibleActions) {
        if (possibleActions.stream().allMatch(actionToTake -> actionToTake instanceof ChooseTargetsAction)) {
            if (possibleActions.getFirst().getDecisionText().equals("Choose card to play from hand")) {
                return possibleActions.stream().filter(action -> action instanceof ChooseTargetsAction chooseTargetsAction
                        && chooseTargetsAction.getTargets().size() == 1
                        && chooseTargetsAction.getTargets().contains(cardToPlayFromHand)).findFirst().orElseThrow();
            }
            return TargetFinderUtil.getBestTarget(possibleActions.stream().map(actionToTake -> ((ChooseTargetsAction) actionToTake)).toList(), copy, playerName);
        }

        // Reveal opponent's hand
        ActionToTake bestRevealHandAction = getBestRevealOpponentsHandAction(possibleActions);
        if (bestRevealHandAction != null) {
            opponentsHandRevealed = true;
            return bestRevealHandAction;
        }

        // Put cards from discard to hand
        ActionToTake bestTakeIntoHandFromDiscardAction = getBestTakeIntoHandFromDiscardAction(possibleActions);
        if (bestTakeIntoHandFromDiscardAction != null) {
            return bestTakeIntoHandFromDiscardAction;
        }

        // Remove burdens
        ActionToTake bestRemoveBurdenAction = getBestRemoveBurdenAction(possibleActions);
        if (bestRemoveBurdenAction != null) {
            return bestRemoveBurdenAction;
        }

        // Heal
        ActionToTake bestHealAction = getBestHealAction(possibleActions);
        if (bestHealAction != null) {
            return bestHealAction;
        }

        // Play fellowship's next site
        ActionToTake bestPlayNextSiteAction = getBestPlayNextSiteAction(possibleActions);
        if (bestPlayNextSiteAction != null) {
            return bestPlayNextSiteAction;
        }

        // Discard cards from play
        ActionToTake bestDiscardCardsFromPlayAction = getBestDiscardCardsFromPlayAction(possibleActions);
        if (bestDiscardCardsFromPlayAction != null) {
            return bestDiscardCardsFromPlayAction;
        }

        // Check for heal companion by discard actions
        Optional<ActionToTake> healAction = possibleActions.stream()
                .filter(actionToTake -> actionToTake instanceof DiscardCompanionToHealAction)
                .findFirst();
        if (healAction.isPresent()) {
            return healAction.get();
        }

        // Play permanents from hand
        PlayCardFromHandAction bestPlayPermanentAction = getBestPlayPermanentFromHandAction(possibleActions);
        if (bestPlayPermanentAction != null) {
            BotCard toPlay = bestPlayPermanentAction.getCard();
            UseCardAction bestPlayPermanentWithBonusAction = getBestPlayPermanentFromHandWithBonusAction(possibleActions, toPlay);
            if (bestPlayPermanentWithBonusAction != null) {
                cardToPlayFromHand = toPlay;
                return bestPlayPermanentWithBonusAction;
            }
            return bestPlayPermanentAction;
        }

        // Unclog hand
        ActionToTake bestUnclogHandAction = getBestUnclogHandAction(possibleActions);
        if (bestUnclogHandAction != null) {
            return bestUnclogHandAction;
        }

        // Finally pass if no other action with value is found
        return possibleActions.stream()
                .filter(actionToTake -> actionToTake instanceof PassAction)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No pass action available. Possible actions: " + possibleActions));
    }

    private UseCardAction getBestPlayPermanentFromHandWithBonusAction(List<ActionToTake> possibleActions, BotCard toPlay) {
        List<UseCardAction> playWithBonusActions = possibleActions.stream()
                .filter(action -> action instanceof UseCardAction useCardAction
                        && useCardAction.getCard().canPlayCardFromHand(Timeword.FELLOWSHIP, toPlay))
                .map(action -> (UseCardAction) action)
                .toList();

        UseCardAction bestAbilityAction = getBestActivatedAbilityAction(playWithBonusActions);
        if (bestAbilityAction != null) {
            double bestAbilityValue = bestAbilityAction.getCard().getActivatedAbility(Timeword.FELLOWSHIP).valueIfActivated(copy, playerName);
            if (bestAbilityValue > 0.0) {
                return bestAbilityAction;
            } else {
                log(2, "No 'play with bonus' action with positive value found among activated abilities. Best ability: " +
                        bestAbilityAction.getCard().getFullName() + " with value " + bestAbilityValue);
            }
        }
        return null;
    }

    private ActionToTake getBestUnclogHandAction(List<ActionToTake> possibleActions) {
        return getBestActionWithAbility(possibleActions,
                BotCard::canUnclogHand,
                "unclog hand");
    }

    private ActionToTake getBestDiscardCardsFromPlayAction(List<ActionToTake> possibleActions) {
        return getBestActionWithAbility(possibleActions,
                BotCard::canDiscardCardsFromPlay,
                "discard cards from play");
    }

    private ActionToTake getBestPlayNextSiteAction(List<ActionToTake> possibleActions) {
        return getBestActionWithAbility(possibleActions,
                BotCard::canPlayNextSite,
                "play fellowship's next site");
    }

    private ActionToTake getBestHealAction(List<ActionToTake> possibleActions) {
        return getBestActionWithAbility(possibleActions,
                BotCard::canHeal,
                "heal");
    }

    private ActionToTake getBestRemoveBurdenAction(List<ActionToTake> possibleActions) {
        return getBestActionWithAbility(possibleActions,
                BotCard::canRemoveBurdens,
                "remove burden");
    }

    private ActionToTake getBestTakeIntoHandFromDiscardAction(List<ActionToTake> possibleActions) {
        return getBestActionWithAbility(possibleActions,
                BotCard::canPutCardsFromDiscardIntoHand,
                "take from discard to hand");
    }

    private ActionToTake getBestRevealOpponentsHandAction(List<ActionToTake> possibleActions) {
        ActionToTake tbr = getBestActionWithAbility(possibleActions,
                BotCard::canRevealOpponentsHand,
                "reveal opponent's hand");

        if (opponentsHandRevealed && tbr != null) {
            log(2, "Opponent's hand already revealed, skipping reveal opponent's hand actions");
            return null;
        }

        return tbr;
    }

    private ActionToTake getBestActionWithAbility(List<ActionToTake> possibleActions,
                                                  BiPredicate<BotCard, Timeword> abilityChecker,
                                                  String actionTypeName) {
        List<PlayCardFromHandAction> eventActions = getEventActionsWithEffect(possibleActions, abilityChecker);
        List<UseCardAction> activatedAbilityActions = getActivatedAbilityActionsWithEffect(possibleActions, abilityChecker);

        PlayCardFromHandAction bestEventAction = getBestEventAction(eventActions);
        if (bestEventAction != null) {
            double bestEventValue = ((BotEventCard) bestEventAction.getCard()).valueIfPlayed(copy, playerName);
            if (bestEventValue > 0.0) {
                return bestEventAction;
            } else {
                log(2, "No '" + actionTypeName + "' action with positive value found among events. Best event: " +
                        bestEventAction.getCard().getFullName() + " with value " + bestEventValue);
            }
        }

        UseCardAction bestAbilityAction = getBestActivatedAbilityAction(activatedAbilityActions);
        if (bestAbilityAction != null) {
            double bestAbilityValue = bestAbilityAction.getCard().getActivatedAbility(Timeword.FELLOWSHIP).valueIfActivated(copy, playerName);
            if (bestAbilityValue > 0.0) {
                return bestAbilityAction;
            } else {
                log(2, "No '" + actionTypeName + "' action with positive value found among activated abilities. Best ability: " +
                        bestAbilityAction.getCard().getFullName() + " with value " + bestAbilityValue);
            }
        }
        return null;
    }

    private List<PlayCardFromHandAction> getEventActionsWithEffect(List<ActionToTake> possibleActions,
                                                                   BiPredicate<BotCard, Timeword> abilityChecker) {
        return possibleActions.stream()
                .filter(action -> action instanceof PlayCardFromHandAction playCardFromHandAction
                        && playCardFromHandAction.getCard() instanceof BotEventCard botEventCard
                        && abilityChecker.test(botEventCard, Timeword.FELLOWSHIP))
                .map(action -> (PlayCardFromHandAction) action)
                .toList();
    }

    private List<UseCardAction> getActivatedAbilityActionsWithEffect(List<ActionToTake> possibleActions,
                                                                     java.util.function.BiPredicate<com.gempukku.lotro.bots.forge.cards.abstractcards.BotCard, Timeword> abilityChecker) {
        return possibleActions.stream()
                .filter(action -> action instanceof UseCardAction useCardAction
                        && abilityChecker.test(useCardAction.getCard(), Timeword.FELLOWSHIP))
                .map(action -> (UseCardAction) action)
                .toList();
    }

    private PlayCardFromHandAction getBestEventAction(List<PlayCardFromHandAction> eventActions) {
        if (eventActions.isEmpty()) {
            return null;
        }
        return eventActions.stream().max((o1, o2) -> Double.compare(
                ((BotEventCard) o1.getCard()).valueIfPlayed(copy, playerName),
                ((BotEventCard) o2.getCard()).valueIfPlayed(copy, playerName)
        )).orElseThrow();
    }

    private UseCardAction getBestActivatedAbilityAction(List<UseCardAction> activatedAbilityActions) {
        if (activatedAbilityActions.isEmpty()) {
            return null;
        }
        return activatedAbilityActions.stream().max((o1, o2) -> Double.compare(
                o1.getCard().getActivatedAbility(Timeword.FELLOWSHIP).valueIfActivated(copy, playerName),
                o2.getCard().getActivatedAbility(Timeword.FELLOWSHIP).valueIfActivated(copy, playerName))
        ).orElseThrow();
    }

    private PlayCardFromHandAction getBestPlayPermanentFromHandAction(List<ActionToTake> possibleActions) {
        List<PlayCardFromHandAction> playPermanentActions = new ArrayList<>(possibleActions.stream()
                .filter(action -> action instanceof PlayCardFromHandAction playCardFromHandAction
                        && !(playCardFromHandAction.getCard() instanceof BotEventCard))
                .sorted(playPermanentComparator())
                .map(action -> (PlayCardFromHandAction) action)
                .toList());

        if (playPermanentActions.isEmpty()) {
            return null;
        }
        return playPermanentActions.getFirst();
    }

    private Comparator<ActionToTake> playPermanentComparator() {
        return (action1, action2) -> {
            if (!(action1 instanceof PlayCardFromHandAction play1) || !(action2 instanceof PlayCardFromHandAction play2)) {
                return 0;
            }

            CardType type1 = play1.getCard().getPhysicalCard().getBlueprint().getCardType();
            CardType type2 = play2.getCard().getPhysicalCard().getBlueprint().getCardType();

            // Define priority order: companion (0), ally (1), possession (2), condition (3)
            int priority1 = getCardTypePriority(type1);
            int priority2 = getCardTypePriority(type2);

            if (priority1 != priority2) {
                return Integer.compare(priority1, priority2);
            }

            // If both are companions, sort by strength (highest first)
            if (type1 == CardType.COMPANION && type2 == CardType.COMPANION) {
                int strength1 = play1.getCard().getPhysicalCard().getBlueprint().getStrength();
                int strength2 = play2.getCard().getPhysicalCard().getBlueprint().getStrength();
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

    @Override
    public String chooseActionToTakeOrPass(DefaultLotroGame game, AwaitingDecision awaitingDecision) {
        log(2, "Fellowship phase plan asked to take action on " + awaitingDecision.toJson().toString(), true);

        if (!isActive()) {
            log(2, "Fellowship plan is outdated");
            throw new IllegalStateException("Plan is outdated");
        }

        if (nextStep >= actions.size()) {
            log(2, "All actions from plan already taken");
            throw new IllegalStateException("All actions from plan already taken");
        }

        ActionToTake action = actions.get(nextStep);
        log(2, "Action " + (nextStep + 1) + " out of " + actions.size() + ": "+ action.toString());
        nextStep++;
        return action.carryOut();
    }

    @Override
    public boolean isOutdated() {
        return !isActive() || nextStep >= actions.size();
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
