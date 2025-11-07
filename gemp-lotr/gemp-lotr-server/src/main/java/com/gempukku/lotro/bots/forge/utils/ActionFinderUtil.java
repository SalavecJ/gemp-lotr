package com.gempukku.lotro.bots.forge.utils;

import com.gempukku.lotro.bots.forge.plan.CombatOutcome;
import com.gempukku.lotro.bots.forge.plan.CombatPath;
import com.gempukku.lotro.bots.forge.plan.endstate.ArcheryPhaseEndState;
import com.gempukku.lotro.bots.forge.plan.endstate.AssignmentPhaseEndState;
import com.gempukku.lotro.bots.forge.plan.endstate.ManeuverPhaseEndState;
import com.gempukku.lotro.bots.forge.plan.endstate.PhaseEndState;
import com.gempukku.lotro.bots.forge.plan.endstate.RegroupPhaseEndState;
import com.gempukku.lotro.bots.forge.plan.endstate.SkirmishPhaseEndState;
import com.gempukku.lotro.bots.forge.plan.endstate.ShadowPhaseEndState;
import com.gempukku.lotro.bots.forge.plan.action.ActionToTake;
import com.gempukku.lotro.bots.forge.plan.action.AssignMinionAction;
import com.gempukku.lotro.bots.forge.plan.action.ChooseSkirmishAction;
import com.gempukku.lotro.bots.forge.plan.action.PassAction;
import com.gempukku.lotro.bots.forge.plan.action.PlayCardFromHandAction;
import com.gempukku.lotro.cards.build.bot.abstractcard.BotCard;
import com.gempukku.lotro.cards.build.bot.abstractcard.BotObjectSupportAreaCard;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.state.PlannedBoardState;

import java.util.*;

public class ActionFinderUtil {

    /**
     * Checks if combat phases should be skipped because there are no minions in play.
     */
    private static boolean shouldSkipToRegroup(PlannedBoardState state) {
        return state.getShadowCardsInPlay(state.getCurrentShadowPlayer()).stream().noneMatch(botCard -> CardType.MINION.equals(botCard.getSelf().getBlueprint().getCardType()));
    }

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

        // Check if there are no minions in play - if so, skip all combat phases
        if (shouldSkipToRegroup(currentState)) {
            // No minions, skip directly to regroup
            // Create empty phase end states for skipped phases (just passing, no actions)
            currentState.moveToNextPhase(); // Move to MANEUVER
            ManeuverPhaseEndState maneuverEndState = new ManeuverPhaseEndState(currentState,
                    List.of(new PassAction()), List.of(new PassAction()));

            currentState.moveToNextPhase(); // Move to ARCHERY
            ArcheryPhaseEndState archeryEndState = new ArcheryPhaseEndState(currentState,
                    List.of(new PassAction()), List.of(new PassAction()));

            currentState.moveToNextPhase(); // Move to ASSIGNMENT
            AssignmentPhaseEndState assignmentEndState = new AssignmentPhaseEndState(currentState,
                    List.of(new PassAction()), List.of(new PassAction()));

            currentState.moveToNextPhase(); // Move to SKIRMISH
            SkirmishPhaseEndState skirmishEndState = new SkirmishPhaseEndState(currentState,
                    List.of(new PassAction()), List.of(new PassAction()));

            currentState.moveToNextPhase(); // Move to REGROUP
            if (!currentState.getCurrentPhase().equals(Phase.REGROUP)) {
                throw new IllegalStateException("Expected REGROUP phase after skipping combat, got " + currentState.getCurrentPhase());
            }
            RegroupPhaseEndState regroupEndState = (RegroupPhaseEndState) findBestCombatPhaseEndState(
                    Phase.REGROUP, currentState, afterShadowPhaseState);

            return new CombatPath(
                    afterShadowPhaseState,
                    maneuverEndState,
                    archeryEndState,
                    assignmentEndState,
                    skirmishEndState,
                    regroupEndState
            );
        }

        // Normal combat flow - there are minions in play
        // Move to maneuver phase and find the best end state
        currentState.moveToNextPhase();
        if (!currentState.getCurrentPhase().equals(Phase.MANEUVER)) {
            throw new IllegalStateException("Expected MANEUVER phase after SHADOW, got " + currentState.getCurrentPhase());
        }
        ManeuverPhaseEndState maneuverEndState = (ManeuverPhaseEndState) findBestCombatPhaseEndState(
                Phase.MANEUVER, currentState, afterShadowPhaseState);

        // Move to archery phase using the maneuver end state
        currentState = new PlannedBoardState(maneuverEndState.getBoardState());
        currentState.moveToNextPhase();
        if (!currentState.getCurrentPhase().equals(Phase.ARCHERY)) {
            throw new IllegalStateException("Expected ARCHERY phase after MANEUVER, got " + currentState.getCurrentPhase());
        }
        ArcheryPhaseEndState archeryEndState = (ArcheryPhaseEndState) findBestCombatPhaseEndState(
                Phase.ARCHERY, currentState, afterShadowPhaseState);

        // Move to assignment phase using the archery end state
        currentState = new PlannedBoardState(archeryEndState.getBoardState());
        currentState.moveToNextPhase();
        if (!currentState.getCurrentPhase().equals(Phase.ASSIGNMENT)) {
            throw new IllegalStateException("Expected ASSIGNMENT phase after ARCHERY, got " + currentState.getCurrentPhase());
        }
        AssignmentPhaseEndState assignmentEndState = (AssignmentPhaseEndState) findBestCombatPhaseEndState(
                Phase.ASSIGNMENT, currentState, afterShadowPhaseState);

        // Move to skirmish phase using the assignment end state
        currentState = new PlannedBoardState(assignmentEndState.getBoardState());
        currentState.moveToNextPhase();
        if (!currentState.getCurrentPhase().equals(Phase.SKIRMISH)) {
            throw new IllegalStateException("Expected SKIRMISH phase after ASSIGNMENT, got " + currentState.getCurrentPhase());
        }
        SkirmishPhaseEndState skirmishEndState = (SkirmishPhaseEndState) findBestCombatPhaseEndState(
                Phase.SKIRMISH, currentState, afterShadowPhaseState);

        // Move to regroup phase using the skirmish end state
        currentState = new PlannedBoardState(skirmishEndState.getBoardState());
        currentState.moveToNextPhase();
        if (!currentState.getCurrentPhase().equals(Phase.REGROUP)) {
            throw new IllegalStateException("Expected REGROUP phase after SKIRMISH, got " + currentState.getCurrentPhase());
        }
        RegroupPhaseEndState regroupEndState = (RegroupPhaseEndState) findBestCombatPhaseEndState(
                Phase.REGROUP, currentState, afterShadowPhaseState);

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
     * Finds the best combat phase end state using minimax exploration with alpha-beta pruning.
     * Both FP and Shadow can play cards during combat phases, so we need to alternate turns.
     * Works for any combat phase (Maneuver, Archery, Assignment, Skirmish, Regroup).
     *
     * @param phase The phase to explore
     * @param currentBoardState The current board state at the start of the phase
     * @param afterShadowPhaseState The board state after shadow phase (needed to calculate combat outcome value)
     * @param alpha Best value Shadow (maximizing player) can guarantee so far
     * @param beta Best value FP (minimizing player) can guarantee so far
     * @return The best PhaseEndState from Shadow's perspective
     */
    private static PhaseEndState findBestCombatPhaseEndState(Phase phase, PlannedBoardState currentBoardState,
                                                             PlannedBoardState afterShadowPhaseState, double alpha, double beta) {
        if (!currentBoardState.getCurrentPhase().equals(phase)) {
            throw new IllegalArgumentException("Current board state must be in " + phase + " phase");
        }

        // Use minimax with alpha-beta pruning to explore all possible outcomes for this phase
        MinimaxResult result = exploreCombatPhaseOptions(currentBoardState, afterShadowPhaseState, new ArrayList<>(), new ArrayList<>(), true, alpha, beta);
        return result.endState;
    }

    /**
     * Finds the best combat phase end state using minimax exploration with alpha-beta pruning.
     * This is a convenience method that uses default alpha-beta bounds.
     */
    public static PhaseEndState findBestCombatPhaseEndState(Phase phase, PlannedBoardState currentBoardState, PlannedBoardState afterShadowPhaseState) {
        return findBestCombatPhaseEndState(phase, currentBoardState, afterShadowPhaseState, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    /**
     * Recursively explores a combat phase using minimax with alpha-beta pruning.
     */
    private static MinimaxResult exploreCombatPhaseOptions(PlannedBoardState currentState, PlannedBoardState afterShadowPhaseState,
                                                           List<ActionToTake> fpActions, List<ActionToTake> shadowActions, boolean isFpTurn,
                                                           double alpha, double beta) {
        // Get possible actions for the current player
        List<ActionToTake> possibleActions = isFpTurn ? getAllPossibleFreePeoplesActions(currentState) : getAllPossibleShadowActions(currentState);

        if (possibleActions.isEmpty()) {
            throw new IllegalStateException("No possible actions found for " + (isFpTurn ? "Free Peoples" : "Shadow") + " player in " + currentState.getCurrentPhase() + " phase");
        }

        MinimaxResult bestResult = null;

        for (ActionToTake action : possibleActions) {
            PlannedBoardState nextState = new PlannedBoardState(currentState);

            // Add action to appropriate history
            if (isFpTurn) {
                fpActions.add(action);
            } else {
                shadowActions.add(action);
            }

            switch (action) {
                case PassAction passAction -> {
                    MinimaxResult result = handlePassAction(nextState, afterShadowPhaseState, fpActions, shadowActions, isFpTurn, alpha, beta);
                    bestResult = updateBestResult(bestResult, result, isFpTurn);
                }
                case AssignMinionAction assignAction -> {
                    MinimaxResult result = handleAssignMinionAction(assignAction, nextState, afterShadowPhaseState, fpActions, shadowActions, isFpTurn, alpha, beta);
                    bestResult = updateBestResult(bestResult, result, isFpTurn);
                }
                case ChooseSkirmishAction chooseSkirmishAction -> {
                    MinimaxResult result = handleChooseSkirmishAction(chooseSkirmishAction, nextState, afterShadowPhaseState, fpActions, shadowActions, alpha, beta);
                    bestResult = updateBestResult(bestResult, result, isFpTurn);
                }
                case null, default ->
                        throw new IllegalStateException("This action is not implemented for combat phase actions");
            }

            // Remove action from history for next iteration
            if (isFpTurn) {
                fpActions.removeLast();
            } else {
                shadowActions.removeLast();
            }

            // Alpha-beta pruning: update alpha/beta and check for cutoff
            if (shouldPrune(bestResult, isFpTurn, alpha, beta)) {
                break;
            }

            // Update alpha/beta bounds for next iteration
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
     * Handles a pass action in the minimax exploration.
     */
    private static MinimaxResult handlePassAction(PlannedBoardState nextState,
                                                  PlannedBoardState afterShadowPhaseState, List<ActionToTake> fpActions,
                                                  List<ActionToTake> shadowActions, boolean isFpTurn, double alpha, double beta) {
        // Check if both players have passed
        boolean fpPassed = !fpActions.isEmpty() && fpActions.getLast() instanceof PassAction;
        boolean shadowPassed = !shadowActions.isEmpty() && shadowActions.getLast() instanceof PassAction;

        if (fpPassed && shadowPassed) {
            // Both players passed - handle special assignment phase cases or create terminal state
            return handleBothPlayersPassedLogic(nextState, afterShadowPhaseState, fpActions, shadowActions, alpha, beta);
        } else {
            // Only one player passed, switch turns and continue
            return exploreCombatPhaseOptions(nextState, afterShadowPhaseState, fpActions, shadowActions, !isFpTurn, alpha, beta);
        }
    }

    /**
     * Handles the logic when both players have passed.
     */
    private static MinimaxResult handleBothPlayersPassedLogic(PlannedBoardState nextState, PlannedBoardState afterShadowPhaseState,
                                                              List<ActionToTake> fpActions, List<ActionToTake> shadowActions,
                                                              double alpha, double beta) {
        // Check for assignment phase special cases
        if (nextState.getCurrentPhase().equals(Phase.ASSIGNMENT) && !nextState.isAssignmentPhasePlayActionsCompleted()) {
            // Special case: In assignment phase, both players passing means we transition to assigning minions
            nextState.setAssignmentPhasePlayActionsCompleted(true);
            // Continue exploration - FP will now start assigning minions
            return exploreCombatPhaseOptions(nextState, afterShadowPhaseState, fpActions, shadowActions, true, alpha, beta);
        } else if (nextState.getCurrentPhase().equals(Phase.ASSIGNMENT) && nextState.isAssignmentPhasePlayActionsCompleted() && !nextState.isFpAssignmentCompleted()) {
            // Special case: FP is done assigning (no more valid assignments available)
            nextState.setFpAssignmentCompleted(true);
            // Continue exploration - Shadow will now assign remaining minions (if any)
            return exploreCombatPhaseOptions(nextState, afterShadowPhaseState, fpActions, shadowActions, false, alpha, beta);
        } else if (nextState.getCurrentPhase().equals(Phase.SKIRMISH)) {
            if (nextState.getCurrentSkirmish() != null) {
                // Resolve the current skirmish
                nextState.resolveCurrentSkirmish();

                // Check if all skirmishes are done
                if (nextState.getAssignments().isEmpty()) {
                    // Move to regroup phase - create terminal state
                    return createTerminalPhaseResult(nextState, afterShadowPhaseState, fpActions, shadowActions, alpha, beta);
                } else {
                    // FP chooses next skirmish - continue exploration
                    return exploreCombatPhaseOptions(nextState, afterShadowPhaseState, fpActions, shadowActions, true, alpha, beta);
                }
            } else {
                throw new IllegalStateException("Current skirmish is null when both players passed in skirmish phase");
            }
        } else {
            // Normal terminal state for this phase
            return createTerminalPhaseResult(nextState, afterShadowPhaseState, fpActions, shadowActions, alpha, beta);
        }
    }

    /**
     * Creates a terminal phase result when both players have passed.
     */
    private static MinimaxResult createTerminalPhaseResult(PlannedBoardState nextState,
                                                           PlannedBoardState afterShadowPhaseState, List<ActionToTake> fpActions,
                                                           List<ActionToTake> shadowActions, double alpha, double beta) {
        // Create the appropriate end state based on the current phase
        PhaseEndState endState = createPhaseEndState(nextState.getCurrentPhase(), nextState, new ArrayList<>(fpActions), new ArrayList<>(shadowActions));

        // To evaluate, we need to get to the regroup phase end state
        double value;
        if (endState instanceof RegroupPhaseEndState regroupEndState) {
            // We're already at regroup - evaluate directly
            CombatOutcome outcome = new CombatOutcome(afterShadowPhaseState, regroupEndState);
            value = outcome.evaluateOutcome();
        } else {
            // We're at an earlier phase - need to continue to next phase to get regroup
            nextState.moveToNextPhase();

            // Check if all minions have been eliminated - if so, skip remaining combat phases
            if (shouldSkipToRegroup(nextState) && !nextState.getCurrentPhase().equals(Phase.REGROUP)) {
                // Skip to regroup phase directly
                while (!nextState.getCurrentPhase().equals(Phase.REGROUP)) {
                    nextState.moveToNextPhase();
                }
            }

            PhaseEndState nextPhaseEndState = findBestCombatPhaseEndState(nextState.getCurrentPhase(), nextState, afterShadowPhaseState, alpha, beta);
            value = nextPhaseEndState.getValue();
        }

        // Store the value in the end state so it can be retrieved later
        endState.setValue(value);
        return new MinimaxResult(endState, value);
    }

    /**
     * Handles an assignment minion action in the minimax exploration.
     */
    private static MinimaxResult handleAssignMinionAction(AssignMinionAction assignAction, PlannedBoardState nextState, PlannedBoardState afterShadowPhaseState, List<ActionToTake> fpActions, List<ActionToTake> shadowActions, boolean isFpTurn, double alpha, double beta) {
        // Apply the assignment action to nextState
        BotCard minion = nextState.getCardById(assignAction.getMinion().getCardId());
        BotCard fpCharacter = nextState.getCardById(assignAction.getFpCharacter().getCardId());
        nextState.assignMinion(minion, fpCharacter);

        // Continue exploration with the same player (they may have more minions to assign)
        return exploreCombatPhaseOptions(nextState, afterShadowPhaseState, fpActions, shadowActions, isFpTurn, alpha, beta);
    }

    /**
     * Handles a choose skirmish action in the minimax exploration.
     */
    private static MinimaxResult handleChooseSkirmishAction(ChooseSkirmishAction chooseSkirmishAction, PlannedBoardState nextState, PlannedBoardState afterShadowPhaseState, List<ActionToTake> fpActions, List<ActionToTake> shadowActions, double alpha, double beta) {
        // Set the current skirmish in nextState based on the chosen FP character
        BotCard fpCharacter = nextState.getCardById(chooseSkirmishAction.getFpCharacter().getCardId());
        nextState.setCurrentSkirmish(fpCharacter);

        // Continue exploration with the same player (FP may choose another skirmish later)
        return exploreCombatPhaseOptions(nextState, afterShadowPhaseState, fpActions, shadowActions, true, alpha, beta);
    }

    /**
     * Checks if alpha-beta pruning should occur.
     */
    private static boolean shouldPrune(MinimaxResult bestResult, boolean isFpTurn, double alpha, double beta) {
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

    /**
     * Helper class to return both the end state and its minimax value.
     */
    private record MinimaxResult(PhaseEndState endState, double value) {
    }

    /**
     * Updates the best result based on minimax logic.
     */
    private static MinimaxResult updateBestResult(MinimaxResult bestResult, MinimaxResult newResult, boolean isFpTurn) {
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

    public static Set<ShadowPhaseEndState> findAllShadowPhaseEndStates(PlannedBoardState plannedBoardState) {
        if (!plannedBoardState.getCurrentPhase().equals(Phase.SHADOW)) {
            throw new IllegalArgumentException("PlannedBoardState must be in SHADOW phase");
        }

        Set<ShadowPhaseEndState> endStates = new HashSet<>();
        exploreShadowPhaseOptions(plannedBoardState, new ArrayList<>(), endStates);
        return endStates;
    }

    private static void exploreShadowPhaseOptions(PlannedBoardState plannedBoardState, ArrayList<ActionToTake> history, Set<ShadowPhaseEndState> endStates) {
        List<ActionToTake> possibleActions = ActionFinderUtil.getAllPossibleShadowActions(plannedBoardState);

        for (ActionToTake action : possibleActions) {
            PlannedBoardState next = new PlannedBoardState(plannedBoardState);
            history.add(action);
            if (action instanceof PassAction) {
                ShadowPhaseEndState endState = new ShadowPhaseEndState(next, history);
                endStates.add(endState);
            } else {
                if (action instanceof PlayCardFromHandAction playCardFromHandAction) {
                    BotCard cardToPlay = next.getCardById(playCardFromHandAction.getCard().getCardId());
                    if (cardToPlay.getSelf().getBlueprint().getCardType().equals(CardType.MINION)) {
                        next.playMinion(cardToPlay);
                    } else if (cardToPlay.getSelf().getBlueprint().getCardType().equals(CardType.CONDITION) && cardToPlay instanceof BotObjectSupportAreaCard) {
                        next.playToShadowSupportArea(cardToPlay);
                    } else {
                        throw new IllegalStateException("Only minion play is implemented in ShadowPlan");
                    }
                } else {
                    throw new IllegalStateException("Only PlayCardFromHandAction is implemented in ShadowPlan");
                }
                exploreShadowPhaseOptions(next, history, endStates);
            }
            history.removeLast();
        }
    }

    private static List<ActionToTake> getAllPossibleFreePeoplesActions(PlannedBoardState plannedBoardState) {
        List<ActionToTake> possibleActions = new ArrayList<>();

        if (plannedBoardState.getCurrentPhase().equals(Phase.MANEUVER)) {
            // TODO maneuver actions
        } else if (plannedBoardState.getCurrentPhase().equals(Phase.ARCHERY)) {
            // TODO archery actions
        } else if (plannedBoardState.getCurrentPhase().equals(Phase.ASSIGNMENT)) {
            if (!plannedBoardState.isAssignmentPhasePlayActionsCompleted()) {
                // Still in the "play events and activate abilities" part
                // TODO assignment phase event/ability actions
                // For now, FP can only pass
            } else if (!plannedBoardState.isFpAssignmentCompleted()) {
                // Both players have passed, now FP assigns minions 1-on-1
                List<ActionToTake> fpAssignmentActions = getFpAssignmentActions(plannedBoardState);
                if (!fpAssignmentActions.isEmpty()) {
                    return fpAssignmentActions;
                }
            }
        } else if (plannedBoardState.getCurrentPhase().equals(Phase.SKIRMISH)) {
            if (plannedBoardState.getCurrentSkirmish() == null) {
                // FP needs to choose which skirmish to resolve next
                List<ActionToTake> chooseSkirmishActions = getChooseSkirmishActions(plannedBoardState);
                if (!chooseSkirmishActions.isEmpty()) {
                    return chooseSkirmishActions;
                }
            } else {
                // Inside a skirmish - FP can use skirmish abilities/events
                // TODO: get skirmish actions for the current skirmish
            }
        } else if (plannedBoardState.getCurrentPhase().equals(Phase.REGROUP)) {
            // TODO regroup actions
        }

        possibleActions.add(new PassAction());
        return possibleActions;
    }

    private static List<ActionToTake> getAllPossibleShadowActions(PlannedBoardState plannedBoardState) {
        List<ActionToTake> possibleActions = new ArrayList<>();

        if (plannedBoardState.getCurrentPhase().equals(Phase.SHADOW)) {
            //TODO other stuff than playing minions
            possibleActions.addAll(getPlayMinionsFromHandActions(plannedBoardState));
            possibleActions.addAll(getPlayShadowConditionsFromHandActions(plannedBoardState));
        } else if (plannedBoardState.getCurrentPhase().equals(Phase.MANEUVER)) {
            // TODO maneuver actions
        } else if (plannedBoardState.getCurrentPhase().equals(Phase.ARCHERY)) {
            // TODO archery actions
        } else if (plannedBoardState.getCurrentPhase().equals(Phase.ASSIGNMENT)) {
            if (!plannedBoardState.isAssignmentPhasePlayActionsCompleted()) {
                // Still in the "play events and activate abilities" part
                // TODO assignment phase event/ability actions
                // For now, Shadow can only pass
            } else if (plannedBoardState.isFpAssignmentCompleted()) {
                // FP has finished assigning, now Shadow assigns remaining unassigned minions
                List<ActionToTake> shadowAssignmentActions = getShadowAssignmentActions(plannedBoardState);
                if (!shadowAssignmentActions.isEmpty()) {
                    return shadowAssignmentActions;
                }
            }
        } else if (plannedBoardState.getCurrentPhase().equals(Phase.SKIRMISH)) {
            // TODO skirmish actions
        } else if (plannedBoardState.getCurrentPhase().equals(Phase.REGROUP)) {
            // TODO regroup actions
        }

        possibleActions.add(new PassAction());
        return possibleActions;
    }

    private static List<ActionToTake> getPlayMinionsFromHandActions(PlannedBoardState plannedBoardState) {
        List<ActionToTake> possibleActions = new ArrayList<>();
        List<BotCard> shadowCardsInHand = plannedBoardState.getHand(plannedBoardState.getCurrentShadowPlayer()).stream()
                .filter(botCard -> Side.SHADOW.equals(botCard.getSelf().getBlueprint().getSide()))
                .filter(botCard -> CardType.MINION.equals(botCard.getSelf().getBlueprint().getCardType()))
                .filter(botCard -> botCard.canBePlayed(plannedBoardState))
                .toList();

        for (BotCard botCard : shadowCardsInHand) {
            int currentSiteNumber = plannedBoardState.getCurrentSite().getSelf().getBlueprint().getSiteNumber();
            int minionSiteNumber = botCard.getSelf().getBlueprint().getSiteNumber();
            boolean roaming = minionSiteNumber > currentSiteNumber;
            int twilightCost = botCard.getSelf().getBlueprint().getTwilightCost();
            if (roaming) {
                twilightCost += 2;
            }
            if (plannedBoardState.getTwilight() >= twilightCost) {
                possibleActions.add(new PlayCardFromHandAction(botCard.getSelf()));
            }
        }

        return possibleActions;
    }

    private static List<ActionToTake> getPlayShadowConditionsFromHandActions(PlannedBoardState plannedBoardState) {
        List<ActionToTake> possibleActions = new ArrayList<>();
        List<BotCard> shadowCardsInHand = plannedBoardState.getHand(plannedBoardState.getCurrentShadowPlayer()).stream()
                .filter(botCard -> Side.SHADOW.equals(botCard.getSelf().getBlueprint().getSide()))
                .filter(botCard -> CardType.CONDITION.equals(botCard.getSelf().getBlueprint().getCardType()))
                .filter(botCard -> botCard.canBePlayed(plannedBoardState))
                .toList();

        for (BotCard botCard : shadowCardsInHand) {
            int twilightCost = botCard.getSelf().getBlueprint().getTwilightCost();
            if (plannedBoardState.getTwilight() >= twilightCost) {
                possibleActions.add(new PlayCardFromHandAction(botCard.getSelf()));
            }
        }

        return possibleActions;
    }

    /**
     * Gets all possible FP assignment actions (assigning minions 1-on-1 to companions/allies).
     */
    private static List<ActionToTake> getFpAssignmentActions(PlannedBoardState plannedBoardState) {
        List<ActionToTake> possibleActions = new ArrayList<>();

        // Get all unassigned minions
        List<BotCard> unassignedMinions = plannedBoardState.getUnassignedMinions();

        // Get all FP characters that can be assigned to (companions and allies at home)
        List<BotCard> validFpCharacters = BoardStateUtil.getCompanionsAndAlliesAtHome(plannedBoardState);

        // Filter out FP characters that already have a minion assigned (FP can only assign 1-on-1)
        validFpCharacters = validFpCharacters.stream()
                .filter(fpChar -> !plannedBoardState.getAssignments().containsKey(fpChar) || plannedBoardState.getAssignments().get(fpChar).isEmpty())
                .toList();

        if (unassignedMinions.isEmpty() || validFpCharacters.isEmpty()) {
            // FP is done, nothing can be assigned anymore
            return possibleActions;
        }

        // For each unassigned minion, generate assignment actions to each valid FP character
        BotCard minionToAssign = unassignedMinions.stream().max(Comparator.comparingInt(plannedBoardState::getStrength)).get();
        for (BotCard fpCharacter : validFpCharacters) {
            possibleActions.add(new AssignMinionAction(minionToAssign.getSelf(), fpCharacter.getSelf()));
        }

        return possibleActions;
    }

    /**
     * Gets all possible Shadow assignment actions (assigning remaining minions).
     */
    private static List<ActionToTake> getShadowAssignmentActions(PlannedBoardState plannedBoardState) {
        List<ActionToTake> possibleActions = new ArrayList<>();

        // Get all unassigned minions
        List<BotCard> unassignedMinions = plannedBoardState.getUnassignedMinions();

        if (unassignedMinions.isEmpty()) {
            // No more minions to assign, Shadow is done
            return possibleActions;
        }

        // Get all FP characters that can be assigned to
        List<BotCard> validFpCharacters = BoardStateUtil.getCompanionsAndAlliesAtHome(plannedBoardState);

        // For the FIRST unassigned minion, generate assignment actions to each valid FP character
        BotCard minionToAssign = unassignedMinions.getFirst();

        for (BotCard fpCharacter : validFpCharacters) {
            possibleActions.add(new AssignMinionAction(minionToAssign.getSelf(), fpCharacter.getSelf()));
        }
        return possibleActions;
    }

    /**
     * Gets all possible ChooseSkirmishAction options for FP.
     * Returns one action for each FP character that has an assignment (unresolved skirmish).
     */
    private static List<ActionToTake> getChooseSkirmishActions(PlannedBoardState plannedBoardState) {
        List<ActionToTake> possibleActions = new ArrayList<>();

        // Get all FP characters that have assignments (these are the unresolved skirmishes)
        for (BotCard fpCharacter : plannedBoardState.getAssignments().keySet()) {
            possibleActions.add(new ChooseSkirmishAction(fpCharacter.getSelf()));
        }


        return possibleActions;
    }
}

