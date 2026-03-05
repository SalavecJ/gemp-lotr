package com.gempukku.lotro.bots.forge.plan;

import com.gempukku.lotro.bots.forge.plan.action.ActionToTake;
import com.gempukku.lotro.bots.forge.plan.action.PassAction;
import com.gempukku.lotro.bots.forge.utils.DecisionToActions;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.DefaultUserFeedback;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.ArrayList;
import java.util.List;

import static com.gempukku.lotro.bots.forge.utils.BotLogging.log;

public class AssignmentPhasePlan implements Plan {
    private final int siteNumber;
    private final String playerName;
    private final boolean fpPlayer;
    private final DefaultLotroGame game;
    private final DefaultLotroGame copy;
    private final DefaultUserFeedback feedback = new DefaultUserFeedback();
    List<ActionToTake> actions = new ArrayList<>();
    private int nextStep = 0;

    public AssignmentPhasePlan(DefaultLotroGame game, String playerName) {
        this.siteNumber = game.getGameState().getCurrentSiteNumber();
        this.playerName = playerName;
        this.fpPlayer = game.getGameState().getCurrentPlayerId().equals(playerName);
        this.game = game;

        log(1, "Making new assignment phase plan for site " + siteNumber, true);

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

            if (chosenAction instanceof PassAction && awaitingDecision.getText().equals("Play Assignment action or Pass")) {
                break;
            }
        }
    }

    private ActionToTake chooseAction(List<ActionToTake> possibleActions) {
        if (possibleActions.size() != 1) {
            throw new IllegalStateException("Unexpected number of possible actions in assignment phase: " + possibleActions.size());
        }
        if (!(possibleActions.getFirst() instanceof PassAction)) {
            throw new IllegalStateException("Unexpected action in assignment phase, only Pass actions available now: " + possibleActions.getFirst());
        }
        return possibleActions.getFirst();
    }

    @Override
    public String chooseActionToTakeOrPass(DefaultLotroGame game, AwaitingDecision awaitingDecision) {
        log(2, "Assignment phase plan asked to take action on " + awaitingDecision.toJson().toString(), true);

        if (!isActive()) {
            log(2, "Assignment plan is outdated");
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
        String name = fpPlayer ? game.getGameState().getCurrentPlayerId() : game.getGameState().getCurrentShadowPlayer();
        return name.equals(playerName)
                && game.getGameState().getCurrentPhase().equals(Phase.ASSIGNMENT)
                && game.getGameState().getCurrentSiteNumber() == siteNumber;
    }

    @Override
    public void decisionMadeByPlayer(AwaitingDecision awaitingDecision, String answer, String player) {
        // Right now only passing is assumed in assignment phase
    }
}
