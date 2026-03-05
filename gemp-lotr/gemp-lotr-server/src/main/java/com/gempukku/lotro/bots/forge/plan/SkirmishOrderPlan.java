package com.gempukku.lotro.bots.forge.plan;

import com.gempukku.lotro.bots.forge.plan.action.ActionToTake;
import com.gempukku.lotro.bots.forge.plan.action.ChooseSkirmishAction;
import com.gempukku.lotro.bots.forge.utils.DecisionToActions;
import com.gempukku.lotro.game.DefaultUserFeedback;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.ArrayList;
import java.util.List;

import static com.gempukku.lotro.bots.forge.utils.BotLogging.log;

public class SkirmishOrderPlan implements Plan {
    private final String playerName;
    private final DefaultLotroGame game;

    private final DefaultLotroGame copy;
    private final DefaultUserFeedback feedback = new DefaultUserFeedback();

    List<ActionToTake> actions = new ArrayList<>();
    private int nextStep = 0;

    public SkirmishOrderPlan(DefaultLotroGame game) {
        this.playerName = game.getGameState().getCurrentPlayerId();
        this.game = game;

        log(1, "Choosing skirmish order", true);

        copy = game.getCopyByReplayingDecisionsFromStart(feedback);

        makePlan();
    }

    private void makePlan() {
        AwaitingDecision awaitingDecision = feedback.getAwaitingDecision(playerName);
        log(2, "Decision expected: " + awaitingDecision.toJson());
        List<ActionToTake> decisionActions = DecisionToActions.toActions(awaitingDecision, copy);
        log(2, "Possible actions: ");
        for (ActionToTake decisionAction : decisionActions) {
            log(2, decisionAction.toString());
        }

        if (decisionActions.stream().anyMatch(action -> !(action instanceof ChooseSkirmishAction))) {
            throw new IllegalStateException("Unexpected action type found among possible actions, only Choose skirmish action is supported: " + decisionActions);
        }

        List<ChooseSkirmishAction> chooseSkirmishActions = decisionActions.stream()
                .map(action -> (ChooseSkirmishAction) action)
                .toList();

        ActionToTake chosenAction = chooseAction(chooseSkirmishActions);

        actions.add(chosenAction);
        log(1, chosenAction.toString());

        String answer = chosenAction.carryOut();
        feedback.participantDecided(playerName, answer);
        try {
            awaitingDecision.decisionMade(answer);
        } catch (DecisionResultInvalidException e) {
            throw new IllegalStateException("Chosen action was invalid: " + answer, e);
        }
        copy.carryOutPendingActionsUntilDecisionNeeded();
    }

    private ChooseSkirmishAction chooseAction(List<ChooseSkirmishAction> possibleActions) {
        ArrayList<ChooseSkirmishAction> sortedActions = new ArrayList<>(possibleActions.stream().sorted((o1, o2) -> {
            int priority1 = getSkirmishPriority(o1);
            int priority2 = getSkirmishPriority(o2);
            return Integer.compare(priority1, priority2);
        }).toList());
        return sortedActions.getFirst();
    }

    /**
     * Get priority for skirmish order. Lower numbers go first.
     */
    private int getSkirmishPriority(ChooseSkirmishAction action) {
        // Ring-bearer first (priority 0)
        if (action.getPhysicalCard().getCardId() == game.getGameState().getRingBearer(playerName).getCardId()) {
            return 0;
        }

        // Boromir, Son of Denethor last (priority 2)
        if (action.getCard().getFullName().equals("Boromir, Son of Denethor")) {
            return 2;
        }

        // Everything else in the middle (priority 1)
        return 1;
    }

    @Override
    public String chooseActionToTakeOrPass(DefaultLotroGame game, AwaitingDecision awaitingDecision) {
        log(2, "Skirmish order plan asked to take action on " + awaitingDecision.toJson().toString(), true);

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
        return nextStep >= actions.size();
    }

    @Override
    public void decisionMadeByPlayer(AwaitingDecision awaitingDecision, String answer, String player) {
        // Right now only passing is assumed in assignment phase
    }
}
