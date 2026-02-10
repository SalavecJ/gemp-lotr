package com.gempukku.lotro.bots.forge.plan.specific;

import com.gempukku.lotro.bots.forge.cards.abstractcards.BotCard;
import com.gempukku.lotro.bots.forge.plan.action.ActionToTake;
import com.gempukku.lotro.bots.forge.plan.action.ChooseOptionAction;
import com.gempukku.lotro.bots.forge.plan.action.ChooseTargetsAction;
import com.gempukku.lotro.bots.forge.utils.WoundsValueUtil;
import com.gempukku.lotro.bots.forge.plan.Plan;
import com.gempukku.lotro.bots.forge.utils.DecisionToActions;
import com.gempukku.lotro.game.DefaultUserFeedback;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.gempukku.lotro.bots.forge.utils.BotLogging.log;

public class MoriaLakePlan implements Plan {
    private final String playerName;
    private final DefaultLotroGame game;
    private final DefaultLotroGame copy;
    private final DefaultUserFeedback feedback = new DefaultUserFeedback();
    List<ActionToTake> actions = new ArrayList<>();
    private int nextStep = 0;

    public MoriaLakePlan(DefaultLotroGame game, AwaitingDecision decision) {
        if (!canMakePlan(game, decision)) {
            throw new IllegalArgumentException("Cannot make Moria Lake plan for given decision: " + decision.toJson());
        }

        this.playerName = game.getGameState().getCurrentPlayerId();
        this.game = game;

        log(1, "Making new plan for Moria Lake", true);

        copy = game.getCopyByReplayingDecisionsFromStart(feedback);

        makePlan();
    }

    public static boolean canMakePlan(DefaultLotroGame game, AwaitingDecision decision) {
        if (!decision.getDecisionParameters().containsKey("source")) {
            return false;
        }

        String source = decision.getDecisionParameters().get("source")[0];
        int sourceId = Integer.parseInt(source);
        if (sourceId <= 0) {
            return false;
        }

        return game.getGameState().getPhysicalCard(sourceId).getBlueprint().getFullName().equals("Moria Lake");
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

            actions.add(chosenAction2);
            log(1, "  " + actions.size() + ". " + chosenAction2);

            String answer = chosenAction2.carryOut();
            feedback.participantDecided(playerName, answer);
            try {
                awaitingDecision.decisionMade(answer);
            } catch (DecisionResultInvalidException e) {
                throw new IllegalStateException("Chosen action was invalid: " + answer, e);
            }
            copy.carryOutPendingActionsUntilDecisionNeeded();

            AwaitingDecision nextAwaitingDecision = feedback.getAwaitingDecision(playerName);
            if (nextAwaitingDecision == null || !canMakePlan(copy, nextAwaitingDecision)) {
                break;
            }
        }
    }

    private ActionToTake chooseAction(List<ActionToTake> possibleActions) {
        if (possibleActions.stream().allMatch(actionToTake -> actionToTake instanceof ChooseOptionAction)) {
            Optional<ActionToTake> exertFrodo = possibleActions.stream().filter(actionToTake -> actionToTake.toString().contains("Exert Frodo")).findFirst();
            if (exertFrodo.isPresent()) {
                return exertFrodo.get();
            } else {
                throw new IllegalStateException("All possible actions are ChooseOptionAction2, but none contains 'Exert Frodo': " + possibleActions);
            }
        }

        if (possibleActions.stream().allMatch(actionToTake -> actionToTake instanceof ChooseTargetsAction)) {
            List<List<BotCard>> targetsList = new ArrayList<>();
            for (ActionToTake possibleAction : possibleActions) {
                ChooseTargetsAction chooseTargetsAction = (ChooseTargetsAction) possibleAction;
                targetsList.add(chooseTargetsAction.getTargets());
            }
            double bestValue = Double.NEGATIVE_INFINITY;
            ActionToTake bestAction = null;
            for (int i = 0; i < possibleActions.size(); i++) {
                List<BotCard> targets = targetsList.get(i);
                double value = 0;
                for (BotCard target : targets) {
                    value += WoundsValueUtil.evaluateWoundsChangeValue(playerName, copy, target.getPhysicalCard(), 1);
                }
                log(3, "Value of action " + possibleActions.get(i) + " is " + value);
                if (value > bestValue) {
                    bestValue = value;
                    bestAction = possibleActions.get(i);
                }
            }
            return bestAction;
        }

        throw new IllegalStateException("Unexpected decision with possible actions: " + possibleActions);
    }

    @Override
    public String chooseActionToTakeOrPass(DefaultLotroGame game, AwaitingDecision awaitingDecision) {
        log(1, "Moria Lake plan asked to take action on " + awaitingDecision.toJson().toString(), true);

        if (nextStep >= actions.size()) {
            log(1, "All actions from plan already taken");
            throw new IllegalStateException("All actions from plan already taken");
        }

        ActionToTake action = actions.get(nextStep);
        log(1, "Action " + (nextStep + 1) + " out of " + actions.size() + ": " + action.toString());
        nextStep++;
        return action.carryOut();
    }

    @Override
    public boolean isOutdated() {
        return nextStep >= actions.size();
    }

    @Override
    public void decisionMadeByPlayer(AwaitingDecision awaitingDecision, String answer, String player) {
        // Plays without interruptions, opponent makes no decisions
    }
}
