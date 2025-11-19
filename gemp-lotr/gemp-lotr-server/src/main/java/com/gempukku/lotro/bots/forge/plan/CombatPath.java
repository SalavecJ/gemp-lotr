package com.gempukku.lotro.bots.forge.plan;

import com.gempukku.lotro.bots.forge.plan.endstate.*;

/**
 * Container for the complete combat path from a shadow phase end state.
 * Contains all 5 combat phase end states: Maneuver -> Archery -> Assignment -> Skirmish -> Regroup
 * The combat outcome can be evaluated by comparing the initial board state (after shadow phase)
 * with the final board state (from the regroup phase end state).
 */
public class CombatPath {
    private final PlannedBoardState initialBoardState;
    private final ManeuverPhaseEndState maneuverPhaseEndState;
    private final ArcheryPhaseEndState archeryPhaseEndState;
    private final AssignmentPhaseEndState assignmentPhaseEndState;
    private final SkirmishPhaseEndState skirmishPhaseEndState;
    private final RegroupPhaseEndState regroupPhaseEndState;

    public CombatPath(
            PlannedBoardState initialBoardState,
            ManeuverPhaseEndState maneuverPhaseEndState,
            ArcheryPhaseEndState archeryPhaseEndState,
            AssignmentPhaseEndState assignmentPhaseEndState,
            SkirmishPhaseEndState skirmishPhaseEndState,
            RegroupPhaseEndState regroupPhaseEndState) {
        this.initialBoardState = new PlannedBoardState(initialBoardState);
        this.maneuverPhaseEndState = maneuverPhaseEndState;
        this.archeryPhaseEndState = archeryPhaseEndState;
        this.assignmentPhaseEndState = assignmentPhaseEndState;
        this.skirmishPhaseEndState = skirmishPhaseEndState;
        this.regroupPhaseEndState = regroupPhaseEndState;
    }

    public PlannedBoardState getInitialBoardState() {
        return initialBoardState;
    }

    public ManeuverPhaseEndState getManeuverPhaseEndState() {
        return maneuverPhaseEndState;
    }

    public ArcheryPhaseEndState getArcheryPhaseEndState() {
        return archeryPhaseEndState;
    }

    public AssignmentPhaseEndState getAssignmentPhaseEndState() {
        return assignmentPhaseEndState;
    }

    public SkirmishPhaseEndState getSkirmishPhaseEndState() {
        return skirmishPhaseEndState;
    }

    public RegroupPhaseEndState getRegroupPhaseEndState() {
        return regroupPhaseEndState;
    }

    /**
     * Gets the combat outcome by comparing the initial board state with the final phase state.
     * If the game ended early (e.g., ring bearer died), uses the last available phase end state.
     */
    public CombatOutcome getCombatOutcome() {
        PlannedBoardState finalBoardState = getLatestPhaseEndState().getBoardState();
        return new CombatOutcome(initialBoardState, finalBoardState);
    }

    /**
     * Gets the latest non-null phase end state.
     * Checks phases in reverse order: Regroup -> Skirmish -> Assignment -> Archery -> Maneuver
     * @return The latest phase end state that is not null
     */
    private PhaseEndState getLatestPhaseEndState() {
        if (regroupPhaseEndState != null) {
            return regroupPhaseEndState;
        }
        if (skirmishPhaseEndState != null) {
            return skirmishPhaseEndState;
        }
        if (assignmentPhaseEndState != null) {
            return assignmentPhaseEndState;
        }
        if (archeryPhaseEndState != null) {
            return archeryPhaseEndState;
        }
        if (maneuverPhaseEndState != null) {
            return maneuverPhaseEndState;
        }
        throw new IllegalStateException("CombatPath has no phase end states");
    }

    /**
     * Evaluates the combat path using the combat outcome evaluation.
     * @return The evaluation score for this combat path
     */
    public double evaluate() {
        return getCombatOutcome().evaluateOutcome();
    }

    @Override
    public String toString() {
        return getCombatOutcome().toString();
    }
}

