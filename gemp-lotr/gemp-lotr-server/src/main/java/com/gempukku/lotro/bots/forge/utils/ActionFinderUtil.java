package com.gempukku.lotro.bots.forge.utils;

import com.gempukku.lotro.bots.forge.plan.CombatOutcome;
import com.gempukku.lotro.bots.forge.plan.action2.*;
import com.gempukku.lotro.bots.forge.plan.endstate.AfterCombatEndState;
import com.gempukku.lotro.bots.forge.plan.endstate.ShadowPhaseEndState;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.communication.UserFeedback;
import com.gempukku.lotro.game.DefaultUserFeedback;
import com.gempukku.lotro.logic.decisions.ActionSelectionDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.*;

public class ActionFinderUtil {

    /**
     * Explores the entire combat tree (Maneuver -> Archery -> Assignment -> Skirmish -> Regroup)
     * from the given board state (after shadow phase) and returns the best combat path.
     * Uses the slow copy method initially to ensure proper setup, then recursively explores all
     * combat phases with fast copies.
     *
     * @param afterShadowPhaseState The board state after shadow phase ends (before combat begins)
     * @return The best AfterCombatEndPhase containing the optimal combat outcome
     */
    public static AfterCombatEndState findBestCombatPath(DefaultLotroGame afterShadowPhaseState) {
        // Do a single combined search through all combat phases

        UserFeedback currentFeedback = new DefaultUserFeedback();
        // Slow copy method to ensure everything is setup correctly, called just once so it's acceptable here
        DefaultLotroGame currentCopy = afterShadowPhaseState.getCopyByReplayingDecisionsFromStart(currentFeedback);

        AfterCombatEndState result = exploreCombatPhases(afterShadowPhaseState, currentCopy, currentFeedback,
                new ArrayList<>(), new ArrayList<>(), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

//        printCombatHistory(afterShadowPhaseState, result.getFpActions(), result.getShadowActions());

        return result;
    }

    /**
     * Recursively explores all combat phases in a single combined search.
     * This avoids redundant computation since evaluating early phases requires looking ahead anyway.
     * Uses fast copying (getCopyForSimulation3) for efficiency during exploration.
     *
     * @param afterShadowPhaseState The original state after shadow phase (for final evaluation)
     * @param currentCopy The current game state at this point in exploration
     * @param currentFeedback The feedback associated with currentCopy
     * @param fpActions FP actions taken so far in the action history
     * @param shadowActions Shadow actions taken so far in the action history
     * @param alpha Best value Shadow can guarantee (for alpha-beta pruning)
     * @param beta Best value FP can guarantee (for alpha-beta pruning)
     * @return The result containing the best combat outcome
     */
    private static AfterCombatEndState exploreCombatPhases(DefaultLotroGame afterShadowPhaseState, DefaultLotroGame currentCopy, UserFeedback currentFeedback,
                                                           List<ActionToTake2> fpActions, List<ActionToTake2> shadowActions,
                                                           double alpha, double beta) {
        // Check if terminal state (before getting decisions)
        String fpPlayerId = currentCopy.getGameState().getCurrentPlayerId();
        String shadowPlayerId = currentCopy.getGameState().getCurrentShadowPlayer();
        AwaitingDecision fpDecision = currentFeedback.getAwaitingDecision(fpPlayerId);
        AwaitingDecision shadowDecision = currentFeedback.getAwaitingDecision(shadowPlayerId);

        if (currentCopy.getWinnerPlayerId() != null
                || (shadowDecision != null && shadowDecision.getText().contains("Reconcile"))
                || (shadowDecision != null && shadowDecision.getText().contains("Choose cards to discard down to 8"))
                || (fpDecision != null && fpDecision.getText().contains("Reconcile"))
                || (fpDecision != null && fpDecision.getText().contains("Choose cards to discard down to 8"))
                || (fpDecision != null && fpDecision.getText().equals("Do you want to make another move?"))) {
            // Terminal state - create end state and return immediately
            return new AfterCombatEndState(currentCopy, fpActions, shadowActions);
        }

        // Get decisions
        boolean isFpTurn = fpDecision != null;
        AwaitingDecision currentDecision = isFpTurn ? fpDecision : shadowDecision;

        // Generate actions
        List<ActionToTake2> possibleActions = DecisionToActions.toActions(currentDecision, currentCopy);

        if (possibleActions.isEmpty()) {
            throw new IllegalStateException("No possible actions found for " + (isFpTurn ? "Free Peoples" : "Shadow") +
                    " player");
        }

        // Special handling for assignment decisions
        if (possibleActions.size() == 1 && possibleActions.getFirst() instanceof AssignMinionsAction2) {
            return exploreAssignmentDecision(afterShadowPhaseState, currentCopy, currentFeedback,
                    fpActions, shadowActions, alpha, beta, isFpTurn);
        }

        // Special handling for skirmish order
        if (possibleActions.stream().allMatch(actionToTake2 -> actionToTake2 instanceof ChooseSkirmishAction2)) {
            possibleActions = List.of(SkirmishOrderUtil.chooseNextSkirmish(currentCopy, possibleActions));
        }

        AfterCombatEndState bestResult = null;

        for (ActionToTake2 action : possibleActions) {
            // Do not explore bad trigger actions
            if (action instanceof AcceptTriggerAction2 acceptAction) {
                if (!acceptAction.getCard().getTriggeredAbility().goodToUse(isFpTurn ? fpPlayerId : shadowPlayerId, currentCopy)
                        && acceptAction.getCard().getTriggeredAbility().isOptionalTrigger()) {
                    continue;
                }
            }

            // Add action to appropriate history
            if (isFpTurn) {
                fpActions.add(action);
            } else {
                shadowActions.add(action);
            }

            // Carry out the chosen action on a fresh copy
            UserFeedback nextFeedback = new DefaultUserFeedback();
            DefaultLotroGame nextState = currentCopy.getCopyByReplayingDecisionsFromLastCheckpoint(nextFeedback);
            AwaitingDecision decision = nextFeedback.getAwaitingDecision(isFpTurn ? fpPlayerId : shadowPlayerId);
            String answer = action.carryOut();
            nextFeedback.participantDecided(isFpTurn ? fpPlayerId : shadowPlayerId, answer);
            try {
                decision.decisionMade(answer);
            } catch (DecisionResultInvalidException e) {
                throw new IllegalStateException("Failed to execute action: " + answer + "; on decision: " + decision.toJson(), e);
            }
            nextState.carryOutPendingActionsUntilDecisionNeeded();

            // Recurse immediately - if terminal, the recursion will return the end state
            AfterCombatEndState result = exploreCombatPhases(afterShadowPhaseState, nextState, nextFeedback,
                    fpActions, shadowActions, alpha, beta);

            // Evaluation
            bestResult = updateBestCombatPathResult(afterShadowPhaseState, bestResult, result, isFpTurn);

            // Remove action from history for next iteration
            if (isFpTurn) {
                fpActions.removeLast();
            } else {
                shadowActions.removeLast();
            }

            // Pruning
            if (shouldPruneCombatPath(afterShadowPhaseState, bestResult, isFpTurn, alpha, beta)) {
                break;
            }

            // Update alpha/beta bounds
            if (bestResult != null) {
                if (isFpTurn) {
                    beta = Math.min(beta, new CombatOutcome(afterShadowPhaseState, bestResult.getGameCopy()).evaluateOutcome());
                } else {
                    alpha = Math.max(alpha, new CombatOutcome(afterShadowPhaseState, bestResult.getGameCopy()).evaluateOutcome());
                }
            }
        }

        return bestResult;
    }

    /**
     * Updates the best result based on minimax logic for combat path exploration.
     */
    private static AfterCombatEndState updateBestCombatPathResult(DefaultLotroGame afterShadowPhaseState, AfterCombatEndState bestResult, AfterCombatEndState newResult, boolean isFpTurn) {
        if (bestResult == null) {
            return newResult;
        }

        if (isFpTurn) {
            // FP minimizes - choose lower value
            return new CombatOutcome(afterShadowPhaseState, newResult.getGameCopy()).evaluateOutcome() < new CombatOutcome(afterShadowPhaseState, bestResult.getGameCopy()).evaluateOutcome() ? newResult : bestResult;
        } else {
            // Shadow maximizes - choose higher value
            return new CombatOutcome(afterShadowPhaseState, newResult.getGameCopy()).evaluateOutcome() > new CombatOutcome(afterShadowPhaseState, bestResult.getGameCopy()).evaluateOutcome() ? newResult : bestResult;
        }
    }

    /**
     * Checks if alpha-beta pruning should occur for combat path exploration.
     */
    private static boolean shouldPruneCombatPath(DefaultLotroGame afterShadowPhaseState, AfterCombatEndState bestResult, boolean isFpTurn, double alpha, double beta) {
        if (bestResult == null) {
            return false;
        }

        if (isFpTurn) {
            // FP is minimizing - check if beta <= alpha
            double newBeta = Math.min(beta, new CombatOutcome(afterShadowPhaseState, bestResult.getGameCopy()).evaluateOutcome());
            return newBeta <= alpha;
        } else {
            // Shadow is maximizing - check if alpha >= beta
            double newAlpha = Math.max(alpha, new CombatOutcome(afterShadowPhaseState, bestResult.getGameCopy()).evaluateOutcome());
            return newAlpha >= beta;
        }
    }

    public static Set<ShadowPhaseEndState> findAllShadowPhaseEndStates(DefaultLotroGame game) {
        if (!game.getGameState().getCurrentPhase().equals(Phase.SHADOW)) {
            throw new IllegalArgumentException("Game must be in SHADOW phase");
        }

        Set<ShadowPhaseEndState> endStates = new HashSet<>();

        // Slow copy method to ensure everything is setup correctly, called just once so it's acceptable here
        UserFeedback currentFeedback = new DefaultUserFeedback();
        DefaultLotroGame currentCopy = game.getCopyByReplayingDecisionsFromStart(currentFeedback);

        exploreShadowPhaseOptions(currentCopy, currentFeedback, new ArrayList<>(), endStates, new ArrayList<>());
        return endStates;
    }

    private static void exploreShadowPhaseOptions(DefaultLotroGame currentCopy, UserFeedback currentFeedback, ArrayList<ActionToTake2> history, Set<ShadowPhaseEndState> endStates,
                                                  List<String> cardsToNotPlay) {
        // Get available actions at the current state
        AwaitingDecision currentDecision = currentFeedback.getAwaitingDecision(currentCopy.getGameState().getCurrentShadowPlayer());
        while (currentDecision == null) {
            AwaitingDecision fpDecision = currentFeedback.getAwaitingDecision(currentCopy.getGameState().getCurrentPlayerId());
            if (fpDecision instanceof ActionSelectionDecision) {
                // Auto-resolve FP required responses
                if (fpDecision.getText().equals("Required responses")) {
                    // FP needs to order triggers - since order doesn't matter, just pick the first one
                    String answer = String.valueOf(0);
                    currentFeedback.participantDecided(currentCopy.getGameState().getCurrentPlayerId(), answer);
                    try {
                        fpDecision.decisionMade(answer);
                    } catch (DecisionResultInvalidException e) {
                        throw new IllegalStateException("Failed to execute FP required response: " + answer, e);
                    }
                    currentCopy.carryOutPendingActionsUntilDecisionNeeded();

                    currentDecision = currentFeedback.getAwaitingDecision(currentCopy.getGameState().getCurrentShadowPlayer());
                } else {
                    throw new IllegalStateException("Expected shadow decision, but got none. FP decision: " + fpDecision.toJson());
                }
            } else {
                throw new IllegalStateException("Expected shadow decision, but got none. FP decision: " + (fpDecision != null ? fpDecision.toJson() : "null"));
            }
        }

        List<ActionToTake2> allActions = DecisionToActions.toActions(currentDecision, currentCopy);
        List<ActionToTake2> possibleActions = new ArrayList<>();

        for (ActionToTake2 availableAction : allActions) {
            switch (availableAction) {
                case PlayCardFromHandAction2 playCardFromHandAction -> {
                    if (cardsToNotPlay.contains(playCardFromHandAction.getCard().getSelf().getBlueprint().getFullName())) {
                        // Skip playing cards that are marked to not play
                        continue;
                    }
                    if (possibleActions.stream().noneMatch(action -> action instanceof PlayCardFromHandAction2 otherPlayCard
                            && playCardFromHandAction.playsTheSameCard(otherPlayCard))) {
                        possibleActions.add(availableAction);
                    } else {
                        // Skip duplicate play card actions for the same card
                    }
                }
                case AcceptTriggerAction2 acceptAction -> {
                    possibleActions.add(acceptAction);
                    if (!acceptAction.getCard().getTriggeredAbility().goodToUse(currentCopy.getGameState().getCurrentShadowPlayer(), currentCopy)
                            && possibleActions.contains(new DenyTriggerAction2(acceptAction.getDecisionText()))) {
                        possibleActions.add(new DenyTriggerAction2(acceptAction.getDecisionText()));
                    }
                }
                case DenyTriggerAction2 ignored -> {
                    // Skip, only add if triggers are not good to use
                }
                case ChooseTargetsAction2 chooseTargetsAction -> {
                    ChooseTargetsAction2 chosen = TargetFinderUtil.getBestTarget(allActions.stream().map(actionToTake2 -> ((ChooseTargetsAction2) actionToTake2)).toList(), currentCopy, currentCopy.getGameState().getCurrentShadowPlayer());
                    if (chosen.equals(chooseTargetsAction)) {
                        possibleActions.add(availableAction);
                    }
                }
                case null, default -> possibleActions.add(availableAction);
            }
        }

        for (ActionToTake2 action : possibleActions) {
            history.add(action);
            int sizeOfCardsToNotPlayBefore = cardsToNotPlay.size();

            UserFeedback nextFeedback = new DefaultUserFeedback();
            DefaultLotroGame nextState = currentCopy.getCopyByReplayingDecisionsFromLastCheckpoint(nextFeedback);
            AwaitingDecision decision = nextFeedback.getAwaitingDecision(nextState.getGameState().getCurrentShadowPlayer());
            String answer = action.carryOut();
            nextFeedback.participantDecided(nextState.getGameState().getCurrentShadowPlayer(), answer);
            try {
                decision.decisionMade(answer);
            } catch (DecisionResultInvalidException e) {
                throw new IllegalStateException("Chosen action was invalid: " + answer, e);
            }
            nextState.carryOutPendingActionsUntilDecisionNeeded();

            if (action instanceof PassAction2 && currentDecision.getText().equals("Play Shadow action or Pass")) {
                // For pass action, create end state
                ShadowPhaseEndState endState = new ShadowPhaseEndState(nextState, history);
                endStates.add(endState);
            } else {
                if (action instanceof PlayCardFromHandAction2 playCardFromHandAction) {
                    // Play events first to avoid exploring unnecessary branches
                    if (playCardFromHandAction.getCard().getSelf().getBlueprint().getCardType() != CardType.EVENT) {
                        for (ActionToTake2 possibleAction : possibleActions) {
                            if (possibleAction instanceof PlayCardFromHandAction2 otherPlayCardAction) {
                                if (otherPlayCardAction.getCard().getSelf().getBlueprint().getCardType() == CardType.EVENT) {
                                    cardsToNotPlay.add(otherPlayCardAction.getCard().getSelf().getBlueprint().getFullName());
                                }
                            }
                        }
                    }
                    // Play conditions after events to avoid exploring unnecessary branches
                    if (playCardFromHandAction.getCard().getSelf().getBlueprint().getCardType() != CardType.CONDITION
                            && playCardFromHandAction.getCard().getSelf().getBlueprint().getCardType() != CardType.EVENT) {
                        for (ActionToTake2 possibleAction : possibleActions) {
                            if (possibleAction instanceof PlayCardFromHandAction2 otherPlayCardAction) {
                                if (otherPlayCardAction.getCard().getSelf().getBlueprint().getCardType() == CardType.CONDITION) {
                                    cardsToNotPlay.add(otherPlayCardAction.getCard().getSelf().getBlueprint().getFullName());
                                }
                            }
                        }
                    }
                    // Play possessions last to avoid exploring unnecessary branches
                    if (playCardFromHandAction.getCard().getSelf().getBlueprint().getCardType() == CardType.POSSESSION) {
                        for (ActionToTake2 possibleAction : possibleActions) {
                            if (possibleAction instanceof PlayCardFromHandAction2 otherPlayCardAction) {
                                if (otherPlayCardAction.getCard().getSelf().getBlueprint().getCardType() != CardType.POSSESSION) {
                                    cardsToNotPlay.add(otherPlayCardAction.getCard().getSelf().getBlueprint().getFullName());
                                }
                            }
                        }
                    }
                }

                if (action instanceof UseCardAction2) {
                    // Activate abilities after playing cards to avoid branching too much
                    for (ActionToTake2 possibleAction : possibleActions) {
                        if (possibleAction instanceof PlayCardFromHandAction2 playCardAction) {
                            cardsToNotPlay.add(playCardAction.getCard().getSelf().getBlueprint().getFullName());
                        }
                    }
                }

                // Recurse with the next state after executing the action
                exploreShadowPhaseOptions(nextState, nextFeedback, history, endStates, cardsToNotPlay);
            }

            while (cardsToNotPlay.size() > sizeOfCardsToNotPlayBefore) {
                cardsToNotPlay.removeLast();
            }
            history.removeLast();
        }
    }

    /**
     * Explores assignment decisions incrementally, one minion at a time.
     * This reduces the branching factor from O(n^m) to O(n*m) and enables better alpha-beta pruning.
     *
     * @param afterShadowPhaseState Original state after shadow phase
     * @param fpActions FP action history
     * @param shadowActions Shadow action history
     * @param alpha Alpha bound for pruning
     * @param beta Beta bound for pruning
     * @param isFpTurn Whether it's FP's turn
     * @return The best result after exploring all assignment branches
     */
    private static AfterCombatEndState exploreAssignmentDecision(DefaultLotroGame afterShadowPhaseState,
                                                                 DefaultLotroGame currentCopy,
                                                                 UserFeedback currentFeedback,
                                                                 List<ActionToTake2> fpActions,
                                                                 List<ActionToTake2> shadowActions,
                                                                 double alpha, double beta,
                                                                 boolean isFpTurn) {

        // Get the assignment action from the current game state
        AwaitingDecision currentDecision = isFpTurn ?
                currentFeedback.getAwaitingDecision(currentCopy.getGameState().getCurrentPlayerId()) :
                currentFeedback.getAwaitingDecision(currentCopy.getGameState().getCurrentShadowPlayer());

        List<ActionToTake2> actions = DecisionToActions.toActions(currentDecision, currentCopy);
        if (actions.size() != 1 || !(actions.getFirst() instanceof AssignMinionsAction2 baseAssignment)) {
            throw new IllegalStateException("Expected single assignment action");
        }

        return exploreAssignmentRecursive(baseAssignment, afterShadowPhaseState, currentCopy,
                fpActions, shadowActions, alpha, beta, isFpTurn);
    }

    /**
     * Recursively explores assignment options by assigning minions one at a time.
     * Uses the current game copy directly for efficiency, avoiding replay overhead.
     *
     * @param assignmentAction Current assignment action (may be partially complete)
     * @param afterShadowPhaseState Original state after shadow phase (for evaluation)
     * @param currentCopy Current game state to copy from
     * @param fpActions FP action history
     * @param shadowActions Shadow action history
     * @param alpha Alpha bound for pruning
     * @param beta Beta bound for pruning
     * @param isFpTurn Whether it's FP's turn
     * @return The best result from this branch
     */
    private static AfterCombatEndState exploreAssignmentRecursive(AssignMinionsAction2 assignmentAction,
                                                                  DefaultLotroGame afterShadowPhaseState,
                                                                  DefaultLotroGame currentCopy,
                                                                  List<ActionToTake2> fpActions,
                                                                  List<ActionToTake2> shadowActions,
                                                                  double alpha, double beta,
                                                                  boolean isFpTurn) {
        // Base case: assignment is complete, execute it
        if (assignmentAction.isComplete()) {
            // Add assignment to history
            if (isFpTurn) {
                fpActions.add(assignmentAction);
            } else {
                shadowActions.add(assignmentAction);
            }

            // Carry out the completed assignment on a fresh copy
            UserFeedback nextFeedback = new DefaultUserFeedback();
            DefaultLotroGame nextState = currentCopy.getCopyByReplayingDecisionsFromLastCheckpoint(nextFeedback);
            AwaitingDecision decision = nextFeedback.getAwaitingDecision(isFpTurn ? currentCopy.getGameState().getCurrentPlayerId() : currentCopy.getGameState().getCurrentShadowPlayer());
            String answer = assignmentAction.carryOut();
            nextFeedback.participantDecided(isFpTurn ? currentCopy.getGameState().getCurrentPlayerId() : currentCopy.getGameState().getCurrentShadowPlayer(), answer);
            try {
                decision.decisionMade(answer);
            } catch (DecisionResultInvalidException e) {
                throw new IllegalStateException("Failed to execute action: " + answer + "; on decision: " + decision.toJson(), e);
            }
            nextState.carryOutPendingActionsUntilDecisionNeeded();

            // Get back to exploring combat phases
            AfterCombatEndState result = exploreCombatPhases(afterShadowPhaseState, nextState, nextFeedback, fpActions, shadowActions, alpha, beta);

            // Remove from history
            if (isFpTurn) {
                fpActions.removeLast();
            } else {
                shadowActions.removeLast();
            }

            return result;
        }

        // Recursive case: explore each possible assignment for the next minion
        List<AssignMinionsAction2.SubAction> availableSubActions = assignmentAction.getAvailableActions();
        AfterCombatEndState bestResult = null;

        for (AssignMinionsAction2.SubAction subAction : availableSubActions) {
            // Create a copy of the assignment action for this branch
            AssignMinionsAction2 branchAssignment = new AssignMinionsAction2(assignmentAction);

            // Assign this minion in the copied action
            branchAssignment.assign(subAction);

            // Recursively explore with the updated assignment
            AfterCombatEndState result = exploreAssignmentRecursive(branchAssignment,
                    afterShadowPhaseState, currentCopy, fpActions, shadowActions, alpha, beta, isFpTurn);

            bestResult = updateBestCombatPathResult(afterShadowPhaseState, bestResult, result, isFpTurn);

            // Alpha-beta pruning
            if (shouldPruneCombatPath(afterShadowPhaseState, bestResult, isFpTurn, alpha, beta)) {
                break;
            }

            // Update alpha/beta bounds
            if (bestResult != null) {
                if (isFpTurn) {
                    beta = Math.min(beta, new CombatOutcome(afterShadowPhaseState, bestResult.getGameCopy()).evaluateOutcome());
                } else {
                    alpha = Math.max(alpha, new CombatOutcome(afterShadowPhaseState, bestResult.getGameCopy()).evaluateOutcome());
                }
            }
        }

        return bestResult;
    }

    /**
     * Prints the complete decision and action history by replaying all actions from the after shadow phase state.
     */
    private static void printCombatHistory(DefaultLotroGame afterShadowPhaseState, List<ActionToTake2> fpActions, List<ActionToTake2> shadowActions) {
        System.out.println("\n=== Combat History Replay ===");
        System.out.println("FP Actions: " + fpActions.size());
        System.out.println("Shadow Actions: " + shadowActions.size());

        DefaultUserFeedback feedback = new DefaultUserFeedback();
        DefaultLotroGame replayState = afterShadowPhaseState.getCopyByReplayingDecisionsFromStart(feedback);

        int fpIndex = 0;
        int shadowIndex = 0;
        int stepNumber = 1;

        while (fpIndex < fpActions.size() || shadowIndex < shadowActions.size()) {
            String fpPlayerId = replayState.getGameState().getCurrentPlayerId();
            String shadowPlayerId = replayState.getGameState().getCurrentShadowPlayer();

            AwaitingDecision fpDecision = feedback.getAwaitingDecision(fpPlayerId);
            AwaitingDecision shadowDecision = feedback.getAwaitingDecision(shadowPlayerId);

            System.out.println("\n--- Step " + stepNumber++ + " ---");
            System.out.println("Phase: " + replayState.getGameState().getCurrentPhase());

            if (fpDecision != null && fpIndex < fpActions.size()) {
                ActionToTake2 fpAction = fpActions.get(fpIndex);
                System.out.println("Decision (FP): " + fpDecision.toJson());
                System.out.println("Action taken: " + fpAction);

                String answer = fpAction.carryOut();
                feedback.participantDecided(fpPlayerId, answer);
                try {
                    fpDecision.decisionMade(answer);
                } catch (DecisionResultInvalidException e) {
                    System.out.println("ERROR: Invalid decision - " + e.getMessage());
                }
                replayState.carryOutPendingActionsUntilDecisionNeeded();
                fpIndex++;
            } else if (shadowDecision != null && shadowIndex < shadowActions.size()) {
                ActionToTake2 shadowAction = shadowActions.get(shadowIndex);
                System.out.println("Decision (Shadow): " + shadowDecision.toJson());
                System.out.println("Action taken: " + shadowAction);

                String answer = shadowAction.carryOut();
                feedback.participantDecided(shadowPlayerId, answer);
                try {
                    shadowDecision.decisionMade(answer);
                } catch (DecisionResultInvalidException e) {
                    System.out.println("ERROR: Invalid decision - " + e.getMessage());
                }
                replayState.carryOutPendingActionsUntilDecisionNeeded();
                shadowIndex++;
            } else {
                System.out.println("No more decisions to replay");
                break;
            }
        }

        System.out.println("\n--- Final State ---");
        System.out.println("Phase: " + replayState.getGameState().getCurrentPhase());
        System.out.println("Winner: " + replayState.getWinnerPlayerId());
        System.out.println("===============================\n");
    }
}
