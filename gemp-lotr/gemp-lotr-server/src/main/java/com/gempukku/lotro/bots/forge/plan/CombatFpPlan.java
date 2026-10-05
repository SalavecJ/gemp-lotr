package com.gempukku.lotro.bots.forge.plan;

import com.gempukku.lotro.bots.forge.cards.BotCardFactory;
import com.gempukku.lotro.bots.forge.cards.abstractcards.BotCard;
import com.gempukku.lotro.bots.forge.plan.action.*;
import com.gempukku.lotro.bots.forge.utils.DecisionToActions;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.DefaultUserFeedback;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static com.gempukku.lotro.bots.forge.utils.BotLogging.log;

public class CombatFpPlan implements Plan {
    private final int siteNumber;
    private final String playerName;
    private final String shadowName;
    private final DefaultLotroGame game;
    private DefaultLotroGame copy;
    private DefaultUserFeedback feedback = new DefaultUserFeedback();
    List<ActionToTake> actions = new ArrayList<>();
    private int nextStep = 0;
    List<ActionToTake> expectedShadowActions = new ArrayList<>();
    private int nextShadowStep = 0;
    private boolean invalidatedByShadowPlayerDecision = false;

    public CombatFpPlan(DefaultLotroGame game) {
        this.siteNumber = game.getGameState().getCurrentSiteNumber();
        this.playerName = game.getGameState().getCurrentPlayerId();
        this.shadowName = game.getGameState().getCurrentShadowPlayer();
        this.game = game;

        makePlan();
    }

    private void makePlan() {
        log(1, "Making new combat plan for site " + siteNumber, true);
        actions.clear();
        nextStep = 0;
        expectedShadowActions.clear();
        nextShadowStep = 0;
        invalidatedByShadowPlayerDecision = false;
        feedback = new DefaultUserFeedback();
        copy = game.getCopyByReplayingDecisionsFromStart(feedback);

        List<BotCard> fpPlayableCards = findCombatPlayableCards(playerName);
        if (fpPlayableCards.isEmpty()) {
            log(1, "No FP playable cards found");
        } else {
            fpPlayableCards.forEach(card -> log(1, "FP playable card: " + card.getFullName()));
        }

        List<BotCard> shadowPlayableCards = findCombatPlayableCards(shadowName);
        if (shadowPlayableCards.isEmpty()) {
            log(1, "No Shadow playable cards found");
        } else {
            shadowPlayableCards.forEach(card -> log(1, "Shadow playable card: " + card.getFullName()));
        }


        while (true) {
            AwaitingDecision fpDecision = feedback.getAwaitingDecision(playerName);
            AwaitingDecision shadowDecision = feedback.getAwaitingDecision(shadowName);

            if (fpDecision != null) {
                handleFpDecision(fpDecision);
            } else if (shadowDecision != null) {
                handleShadowDecision(shadowDecision);
            } else {
                throw new IllegalStateException("No decision found for either player");
            }

            copy.carryOutPendingActionsUntilDecisionNeeded();

            if (copy.isFinished() || copy.getGameState().getCurrentPlayerId().equals(shadowName) || copy.getGameState().getCurrentSiteNumber() != siteNumber) {
                break;
            }
        }
    }

    private List<BotCard> findCombatPlayableCards(String playerName) {
        boolean isFp = playerName.equals(this.playerName);
        return copy.getGameState().getHand(playerName).stream()
                .filter((Predicate<PhysicalCard>) physicalCard -> physicalCard.getBlueprint().getSide() == (isFp ? Side.FREE_PEOPLE : Side.SHADOW))
                .map(BotCardFactory::create)
                .filter(BotCard::canBePlayedFromHandDuringCombat)
                .toList();
    }

    private void handleFpDecision(AwaitingDecision awaitingDecision) {
        log(1, "Decision expected: " + awaitingDecision.toJson());
        List<ActionToTake> decisionActions = DecisionToActions.toActions(awaitingDecision, copy);
        log(1, "Possible actions: ");
        for (ActionToTake decisionAction : decisionActions) {
            if (decisionAction instanceof AssignMinionsAction assignMinionsAction) {
                String fpCharacters = assignMinionsAction.getFpCharactersToAssignTo().stream()
                        .map(BotCard::getFullName)
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("");
                String minions = assignMinionsAction.getMinionsToAssign().stream()
                        .map(BotCard::getFullName)
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("");
                log(1, "FP assignment decision (FP characters: " + fpCharacters + "; Minions: " + minions + ")");
            } else {
                log(1, decisionAction.toString());
            }
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
    }

    private void handleShadowDecision(AwaitingDecision awaitingDecision) {
        log(1, "Shadow decision expected: " + awaitingDecision.toJson());
        List<ActionToTake> decisionActions = DecisionToActions.toActions(awaitingDecision, copy);
        log(1, "Shadow possible actions: ");
        for (ActionToTake decisionAction : decisionActions) {
            if (decisionAction instanceof AssignMinionsAction assignMinionsAction) {
                String fpCharacters = assignMinionsAction.getFpCharactersToAssignTo().stream()
                        .map(BotCard::getFullName)
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("");
                String minions = assignMinionsAction.getMinionsToAssign().stream()
                        .map(BotCard::getFullName)
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("");
                log(1, "Shadow assignment decision (FP characters: " + fpCharacters + "; Minions: " + minions + ")");
            } else {
                log(1, decisionAction.toString());
            }
        }
        ActionToTake chosenAction = chooseAction(decisionActions);

        expectedShadowActions.add(chosenAction);
        log(1, "      Shadow action: " + chosenAction);

        String answer = chosenAction.carryOut();
        feedback.participantDecided(shadowName, answer);
        try {
            awaitingDecision.decisionMade(answer);
        } catch (DecisionResultInvalidException e) {
            throw new IllegalStateException("Chosen action was invalid: " + answer, e);
        }
    }

    private ActionToTake chooseAction(List<ActionToTake> decisionActions) {
        if (decisionActions.size() == 1 && decisionActions.getFirst() instanceof AssignMinionsAction assignMinionsAction) {
            int actionNumber = 1;
            while (!assignMinionsAction.isComplete()) {
                log(2, "Possible actions: ");
                for (AssignMinionsAction.SubAction availableAction : assignMinionsAction.getAvailableActions()) {
                    log(2, availableAction.toString());
                }
                List<AssignMinionsAction.SubAction> availableActions = assignMinionsAction.getAvailableActions();
                // random choice
                AssignMinionsAction.SubAction chosenAction = availableActions.get((int) (Math.random() * availableActions.size()));
                log(1, "  " + actionNumber++ + ". " + chosenAction);
                assignMinionsAction.assign(chosenAction);
            }

        }

        Optional<ActionToTake> optionalPass = decisionActions.stream().filter(new Predicate<ActionToTake>() {
            @Override
            public boolean test(ActionToTake action) {
                return action instanceof PassAction;
            }
        }).findFirst();
        return optionalPass.orElseGet(decisionActions::getFirst);
    }

    @Override
    public String chooseActionToTakeOrPass(DefaultLotroGame game, AwaitingDecision awaitingDecision) {
        log(2, "Combat plan asked to take action on " + awaitingDecision.toJson().toString(), true);
        if (invalidatedByShadowPlayerDecision) {
            log(1, "Combat plan invalidated by shadow player decision. Replanning now");
            makePlan();
        }

        if (!isActive()) {
            log(2, "Combat plan is outdated");
            throw new IllegalStateException("Plan is outdated");
        }

        if (nextStep >= actions.size()) {
            log(2, "All actions from plan already taken");
            throw new IllegalStateException("All actions from plan already taken");
        }

        ActionToTake action = actions.get(nextStep);
        log(2, "Action " + (nextStep + 1) + " out of " + actions.size() + ": " + action.toString());
        nextStep++;
        return action.carryOut();
    }

    @Override
    public boolean isOutdated() {
        return !isActive() || nextStep >= actions.size();
    }

    private boolean isActive() {
        return game.getGameState().getCurrentPlayerId().equals(playerName)
                &&
                (game.getGameState().getCurrentPhase().equals(Phase.MANEUVER) ||
                game.getGameState().getCurrentPhase().equals(Phase.ARCHERY) ||
                game.getGameState().getCurrentPhase().equals(Phase.ASSIGNMENT) ||
                game.getGameState().getCurrentPhase().equals(Phase.SKIRMISH) ||
                game.getGameState().getCurrentPhase().equals(Phase.REGROUP))
                && game.getGameState().getCurrentSiteNumber() == siteNumber;
    }

    @Override
    public void decisionMadeByPlayer(DefaultLotroGame game, AwaitingDecision awaitingDecision, String answer, String player) {
        if (!player.equals(shadowName) || invalidatedByShadowPlayerDecision) {
            return;
        }
        if (nextShadowStep >= expectedShadowActions.size()) {
            log(1, "Shadow player made a decision that was not expected by FP plan");
            log(2, "Decision: " + awaitingDecision.toJson());
            log(2, "Answer: " + answer);
            invalidatedByShadowPlayerDecision = true;
            return;
        }

        ActionToTake predictedShadowAction = expectedShadowActions.get(nextShadowStep);
        nextShadowStep++;
        String predictedAnswer = predictedShadowAction.carryOut();
        if (!predictedAnswer.equals(answer)) {
            log(1, "Shadow player made a different decision than FP plan expected - fp taken actions: " + nextStep + " out of " + actions.size());
            log(2, "Predicted answer: " + predictedAnswer);
            log(2, "Actual answer: " + answer);
            invalidatedByShadowPlayerDecision = true;
        }
    }
}
