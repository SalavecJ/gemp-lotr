package com.gempukku.lotro.bots.forge.plan;

import com.gempukku.lotro.bots.forge.plan.endstate.ArcheryPhaseEndState;
import com.gempukku.lotro.bots.forge.plan.endstate.AssignmentPhaseEndState;
import com.gempukku.lotro.bots.forge.plan.endstate.ManeuverPhaseEndState;
import com.gempukku.lotro.bots.forge.plan.endstate.RegroupPhaseEndState;
import com.gempukku.lotro.bots.forge.plan.endstate.SkirmishPhaseEndState;

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
     * Gets the combat outcome by comparing the initial board state with the final regroup state.
     */
    public CombatOutcome getCombatOutcome() {
        return new CombatOutcome(initialBoardState, regroupPhaseEndState);
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

