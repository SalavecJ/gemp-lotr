package com.gempukku.lotro.bots.forge.plan;

import com.gempukku.lotro.bots.forge.plan.action.*;
import com.gempukku.lotro.bots.forge.utils.DecisionToActions;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.DefaultUserFeedback;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.ArrayList;
import java.util.List;

import static com.gempukku.lotro.bots.forge.utils.BotLogging.log;

public class ShadowAssigningPlan implements Plan {
    private final int siteNumber;
    private final String playerName;
    private final DefaultLotroGame game;

    private int nextStep = 0;
    List<ActionToTake> actions = new ArrayList<>();

    private final DefaultLotroGame copy;
    private final DefaultUserFeedback feedback = new DefaultUserFeedback();

    public ShadowAssigningPlan(DefaultLotroGame game) {
        this.siteNumber = game.getGameState().getCurrentSiteNumber();
        this.playerName = game.getGameState().getCurrentShadowPlayer();
        this.game = game;

        log(1, "Making shadow assigning plan for site " + siteNumber, true);

        copy = game.getCopyByReplayingDecisionsFromStart(feedback);

        makePlan();
    }

    private void makePlan() {
        AwaitingDecision awaitingDecision = feedback.getAwaitingDecision(playerName);
        log(2, "Decision expected: " + awaitingDecision.toJson());
        List<ActionToTake> decisionActions = DecisionToActions.toActions(awaitingDecision, copy);
        if (decisionActions.size() != 1) {
            throw new IllegalStateException("Expected exactly 1 possible action for shadow assigning plan, but got " + decisionActions.size());
        }
        if (!(decisionActions.getFirst() instanceof AssignMinionsAction assignMinionsAction)) {
            throw new IllegalStateException("Expected only possible action for shadow assigning plan to be AssignMinionsAction, but got " + decisionActions.getFirst().getClass().getSimpleName());
        }

        int actionNumber = 1;
        while (!assignMinionsAction.isComplete()) {
            log(2, "Possible actions: ");
            for (AssignMinionsAction.SubAction availableAction : assignMinionsAction.getAvailableActions()) {
                log(2, availableAction.toString());
            }
            AssignMinionsAction.SubAction chosenAction = chooseAction(assignMinionsAction.getAvailableActions());
            log(1, "  " + actionNumber++ + ". " + chosenAction);
            assignMinionsAction.assign(chosenAction);
        }

        actions.add(assignMinionsAction);

        String answer = assignMinionsAction.carryOut();
        feedback.participantDecided(playerName, answer);
        try {
            awaitingDecision.decisionMade(answer);
        } catch (DecisionResultInvalidException e) {
            throw new IllegalStateException("Chosen action was invalid: " + answer, e);
        }
        copy.carryOutPendingActionsUntilDecisionNeeded();
    }

    private AssignMinionsAction.SubAction chooseAction(List<AssignMinionsAction.SubAction> availableActions) {
        //TODO something with skirmish prediction
        for (AssignMinionsAction.SubAction availableAction : availableActions) {
            if (game.getGameState().getRingBearer(game.getGameState().getCurrentPlayerId()).getCardId() == availableAction.fpCharacter.getCardId()) {
                log(2, "Choosing " + availableAction + " because it assigns to ring-bearer");
                return availableAction;
            }
        }
        log(2, "No action assigns to ring-bearer, choosing first available " + availableActions.getFirst());
        return availableActions.getFirst();
    }

    @Override
    public String chooseActionToTakeOrPass(DefaultLotroGame game, AwaitingDecision awaitingDecision) {
        log(2, "Shadow assigning plan asked to take action on " + awaitingDecision.toJson().toString(), true);

        if (!isActive()) {
            log(2, "Shadow assigning plan is outdated");
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
        return !game.getGameState().getCurrentPlayerId().equals(playerName)
                && game.getGameState().getCurrentPhase().equals(Phase.ASSIGNMENT)
                && game.getGameState().getCurrentSiteNumber() == siteNumber;
    }

    @Override
    public void decisionMadeByPlayer(AwaitingDecision awaitingDecision, String answer, String player) {
        // During assigning the player plays without interruptions, opponent makes no decisions
    }
}
