package com.gempukku.lotro.bots.forge.utils;

import com.gempukku.lotro.bots.forge.plan.CombatOutcome;
import com.gempukku.lotro.bots.forge.plan.CombatPath;
import com.gempukku.lotro.bots.forge.plan.action.*;
import com.gempukku.lotro.bots.forge.plan.endstate.ArcheryPhaseEndState;
import com.gempukku.lotro.bots.forge.plan.endstate.AssignmentPhaseEndState;
import com.gempukku.lotro.bots.forge.plan.endstate.ManeuverPhaseEndState;
import com.gempukku.lotro.bots.forge.plan.endstate.PhaseEndState;
import com.gempukku.lotro.bots.forge.plan.endstate.RegroupPhaseEndState;
import com.gempukku.lotro.bots.forge.plan.endstate.SkirmishPhaseEndState;
import com.gempukku.lotro.bots.forge.plan.endstate.ShadowPhaseEndState;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.bots.forge.plan.PlannedBoardState;

import java.util.*;

public class ActionFinderUtil {
    /**
     * Explores the entire combat tree (Maneuver -> Archery -> Assignment -> Skirmish -> Regroup)
     * from the given board state (after shadow phase) and returns the best combat path.
     *
     * @param afterShadowPhaseState The board state after shadow phase ends (before combat begins)
     * @return The best CombatPath containing all 5 combat phase end states
     */
    public static CombatPath findBestCombatPath(PlannedBoardState afterShadowPhaseState) {
        // Start with a copy of the after-shadow-phase state
        PlannedBoardState currentState = new PlannedBoardState(afterShadowPhaseState);

        // Do a single combined search through all combat phases
        CombatPathResult result = exploreCombatPhases(currentState, afterShadowPhaseState, new HashMap<>(),
                new ArrayList<>(), new ArrayList<>(), true, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        Map<Phase, PhaseEndState> phaseEndStates = result.phaseEndStates;

        // Extract individual phase end states (they may be null if phase was skipped)
        ManeuverPhaseEndState maneuverEndState = (ManeuverPhaseEndState) phaseEndStates.get(Phase.MANEUVER);
        ArcheryPhaseEndState archeryEndState = (ArcheryPhaseEndState) phaseEndStates.get(Phase.ARCHERY);
        AssignmentPhaseEndState assignmentEndState = (AssignmentPhaseEndState) phaseEndStates.get(Phase.ASSIGNMENT);
        SkirmishPhaseEndState skirmishEndState = (SkirmishPhaseEndState) phaseEndStates.get(Phase.SKIRMISH);
        RegroupPhaseEndState regroupEndState = (RegroupPhaseEndState) phaseEndStates.get(Phase.REGROUP);

        // Create and return the complete combat path
        return new CombatPath(
                afterShadowPhaseState,
                maneuverEndState,
                archeryEndState,
                assignmentEndState,
                skirmishEndState,
                regroupEndState
        );
    }

    /**
     * Recursively explores all combat phases in a single combined search.
     * This avoids redundant computation since evaluating early phases requires looking ahead anyway.
     *
     * @param currentState Current board state
     * @param afterShadowPhaseState The original state after shadow phase (for final evaluation)
     * @param phaseEndStates Map to accumulate phase end states as we transition between phases
     * @param fpActions FP actions taken in the current phase
     * @param shadowActions Shadow actions taken in the current phase
     * @param isFpTurn Whether it's FP's turn
     * @param alpha Best value Shadow can guarantee
     * @param beta Best value FP can guarantee
     * @return The result containing all phase end states and the final value
     */
    private static CombatPathResult exploreCombatPhases(PlannedBoardState currentState, PlannedBoardState afterShadowPhaseState,
                                                        Map<Phase, PhaseEndState> phaseEndStates,
                                                        List<ActionToTake> fpActions, List<ActionToTake> shadowActions,
                                                        boolean isFpTurn, double alpha, double beta) {
        // Get possible actions for the current player
        List<ActionToTake> possibleActions = isFpTurn ?
                currentState.getAvailableActions(currentState.getCurrentFpPlayer()) :
                currentState.getAvailableActions(currentState.getCurrentShadowPlayer());

        if (possibleActions.isEmpty()) {
            throw new IllegalStateException("No possible actions found for " + (isFpTurn ? "Free Peoples" : "Shadow") +
                    " player in " + currentState.getCurrentPhase() + " phase");
        }

        CombatPathResult bestResult = null;

        for (ActionToTake action : possibleActions) {
            PlannedBoardState nextState = new PlannedBoardState(currentState);

            // Add action to appropriate history
            if (isFpTurn) {
                fpActions.add(action);
            } else {
                shadowActions.add(action);
            }

            String playerId = isFpTurn ? nextState.getCurrentFpPlayer() : nextState.getCurrentShadowPlayer();
            Phase beforeAction = nextState.getCurrentPhase();
            nextState.takeAction(playerId, action);
            Phase afterAction = nextState.getCurrentPhase();

            CombatPathResult result;
            if (!beforeAction.equals(afterAction) || nextState.isEndOfTurnProceduresStarted()) {
                // Phase transition occurred - record the end state and continue to next phase
                Phase phaseJustEnded = nextState.isEndOfTurnProceduresStarted() ? Phase.REGROUP : Phase.getPhaseBefore(afterAction);
                PhaseEndState endState = createPhaseEndState(phaseJustEnded, nextState, new ArrayList<>(fpActions), new ArrayList<>(shadowActions));

                // Add to our accumulated phase end states
                Map<Phase, PhaseEndState> newPhaseEndStates = new HashMap<>(phaseEndStates);
                newPhaseEndStates.put(phaseJustEnded, endState);

                if (phaseJustEnded == Phase.REGROUP) {
                    // We've reached the end - evaluate the final outcome
                    CombatOutcome outcome = new CombatOutcome(afterShadowPhaseState, (RegroupPhaseEndState) endState);
                    double value = outcome.evaluateOutcome();
                    endState.setValue(value);
                    result = new CombatPathResult(newPhaseEndStates, value);
                } else {
                    // Continue to the next phase with fresh action lists
                    boolean fpToActNext = nextState.getPlayerToAct().equals(nextState.getCurrentFpPlayer());
                    result = exploreCombatPhases(nextState, afterShadowPhaseState, newPhaseEndStates,
                            new ArrayList<>(), new ArrayList<>(), fpToActNext, alpha, beta);
                    // Set the value on this phase's end state
                    endState.setValue(result.value);
                }
            } else {
                // Same phase - continue exploring
                boolean fpToActNext = nextState.getPlayerToAct().equals(nextState.getCurrentFpPlayer());
                result = exploreCombatPhases(nextState, afterShadowPhaseState, phaseEndStates,
                        fpActions, shadowActions, fpToActNext, alpha, beta);
            }

            bestResult = updateBestCombatPathResult(bestResult, result, isFpTurn);

            // Remove action from history for next iteration
            if (isFpTurn) {
                fpActions.removeLast();
            } else {
                shadowActions.removeLast();
            }

            // Alpha-beta pruning
            if (shouldPruneCombatPath(bestResult, isFpTurn, alpha, beta)) {
                break;
            }

            // Update alpha/beta bounds
            if (bestResult != null) {
                if (isFpTurn) {
                    beta = Math.min(beta, bestResult.value);
                } else {
                    alpha = Math.max(alpha, bestResult.value);
                }
            }
        }

        return bestResult;
    }

    /**
     * Helper record to return both the accumulated phase end states and the final value.
     */
    private record CombatPathResult(Map<Phase, PhaseEndState> phaseEndStates, double value) {
    }

    /**
     * Updates the best result based on minimax logic for combat path exploration.
     */
    private static CombatPathResult updateBestCombatPathResult(CombatPathResult bestResult, CombatPathResult newResult, boolean isFpTurn) {
        if (bestResult == null) {
            return newResult;
        }

        if (isFpTurn) {
            // FP minimizes - choose lower value
            return newResult.value < bestResult.value ? newResult : bestResult;
        } else {
            // Shadow maximizes - choose higher value
            return newResult.value > bestResult.value ? newResult : bestResult;
        }
    }

    /**
     * Checks if alpha-beta pruning should occur for combat path exploration.
     */
    private static boolean shouldPruneCombatPath(CombatPathResult bestResult, boolean isFpTurn, double alpha, double beta) {
        if (bestResult == null) {
            return false;
        }

        if (isFpTurn) {
            // FP is minimizing - check if beta <= alpha
            double newBeta = Math.min(beta, bestResult.value);
            return newBeta <= alpha;
        } else {
            // Shadow is maximizing - check if alpha >= beta
            double newAlpha = Math.max(alpha, bestResult.value);
            return newAlpha >= beta;
        }
    }

    /**
     * Creates the appropriate PhaseEndState based on the current phase.
     */
    private static PhaseEndState createPhaseEndState(Phase phase, PlannedBoardState boardState, List<ActionToTake> fpActions, List<ActionToTake> shadowActions) {
        return switch (phase) {
            case MANEUVER -> new ManeuverPhaseEndState(boardState, fpActions, shadowActions);
            case ARCHERY -> new ArcheryPhaseEndState(boardState, fpActions, shadowActions);
            case ASSIGNMENT -> new AssignmentPhaseEndState(boardState, fpActions, shadowActions);
            case SKIRMISH -> new SkirmishPhaseEndState(boardState, fpActions, shadowActions);
            case REGROUP -> new RegroupPhaseEndState(boardState, fpActions, shadowActions);
            default -> throw new IllegalArgumentException("Unsupported phase: " + phase);
        };
    }

    public static Set<ShadowPhaseEndState> findAllShadowPhaseEndStates(PlannedBoardState plannedBoardState) {
        if (!plannedBoardState.getCurrentPhase().equals(Phase.SHADOW)) {
            throw new IllegalArgumentException("PlannedBoardState must be in SHADOW phase");
        }

        Set<ShadowPhaseEndState> endStates = new HashSet<>();
        exploreShadowPhaseOptions(plannedBoardState, new ArrayList<>(), endStates, new ArrayList<>());
        return endStates;
    }

    private static void exploreShadowPhaseOptions(PlannedBoardState plannedBoardState, ArrayList<ActionToTake> history, Set<ShadowPhaseEndState> endStates,
                                                  List<String> cardsToNotPlay) {
        List<ActionToTake> possibleActions = new ArrayList<>();

        for (ActionToTake availableAction : plannedBoardState.getAvailableActions(plannedBoardState.getCurrentShadowPlayer())) {
            switch (availableAction) {
                case PlayCardFromHandAction playCardFromHandAction -> {
                    if (cardsToNotPlay.contains(playCardFromHandAction.getCard().getSelf().getBlueprint().getFullName())) {
                        // Skip playing cards that are marked to not play
                        continue;
                    }
                    if (possibleActions.stream().noneMatch(action -> action instanceof PlayCardFromHandAction otherPlayCard
                            && playCardFromHandAction.playsTheSameCard(otherPlayCard))) {
                        possibleActions.add(availableAction);
                    } else {
                        // Skip duplicate play card actions for the same card
                    }
                }
                case OptionalTriggerDenyAction denyAction -> {
                    if (!denyAction.getSource().getTriggeredAbility().goodToUseNoMatterWhat(plannedBoardState.getCurrentShadowPlayer(), plannedBoardState)) {
                        possibleActions.add(availableAction);
                    }
                }
                case ChooseTargetForAttachmentAction chooseTargetForAttachmentAction -> {
                    if (possibleActions.stream().noneMatch(action -> action instanceof ChooseTargetForAttachmentAction otherChooseTarget
                            && chooseTargetForAttachmentAction.targetsTheSameTypeOfBearer(otherChooseTarget, plannedBoardState))) {
                        possibleActions.add(availableAction);
                    }
                }
                case ChooseTargetForEffectAction chooseTargetForEffectAction -> {
                    if (possibleActions.stream().noneMatch(action -> action instanceof ChooseTargetForEffectAction otherChooseTarget
                            && chooseTargetForEffectAction.targetsTheSameTypeOfTarget(otherChooseTarget, plannedBoardState))) {
                        possibleActions.add(availableAction);
                    }
                }
                case null, default -> possibleActions.add(availableAction);
            }
        }

        for (ActionToTake action : possibleActions) {
            PlannedBoardState next = new PlannedBoardState(plannedBoardState);

            history.add(action);

            int sizeOfCardsToNotPlayBefore = cardsToNotPlay.size();

            if (action instanceof PassAction) {
                next.takeAction(next.getCurrentShadowPlayer(), action); // move to maneuver phase
                ShadowPhaseEndState endState = new ShadowPhaseEndState(next, history);
                endStates.add(endState);
            } else {

                if(action instanceof PlayCardFromHandAction playCardFromHandAction) {
                    // Play conditions at the first opportunity to avoid exploring unnecessary branches
                    if (playCardFromHandAction.getCard().getSelf().getBlueprint().getCardType() != CardType.CONDITION) {
                        for (ActionToTake possibleAction : possibleActions) {
                            if (possibleAction instanceof PlayCardFromHandAction otherPlayCardAction) {
                                if (otherPlayCardAction.getCard().getSelf().getBlueprint().getCardType() == CardType.CONDITION) {
                                    cardsToNotPlay.add(otherPlayCardAction.getCard().getSelf().getBlueprint().getFullName());
                                }
                            }
                        }
                    }
                    // Play possessions last to avoid exploring unnecessary branches
                    if (playCardFromHandAction.getCard().getSelf().getBlueprint().getCardType() == CardType.POSSESSION) {
                        for (ActionToTake possibleAction : possibleActions) {
                            if (possibleAction instanceof PlayCardFromHandAction otherPlayCardAction) {
                                if (otherPlayCardAction.getCard().getSelf().getBlueprint().getCardType() != CardType.POSSESSION) {
                                    cardsToNotPlay.add(otherPlayCardAction.getCard().getSelf().getBlueprint().getFullName());
                                }
                            }
                        }
                    }
                }

                next.takeAction(next.getCurrentShadowPlayer(), action);
                exploreShadowPhaseOptions(next, history, endStates, cardsToNotPlay);
            }

            while (cardsToNotPlay.size() > sizeOfCardsToNotPlayBefore) {
                cardsToNotPlay.removeLast();
            }

            history.removeLast();
        }
    }
}

