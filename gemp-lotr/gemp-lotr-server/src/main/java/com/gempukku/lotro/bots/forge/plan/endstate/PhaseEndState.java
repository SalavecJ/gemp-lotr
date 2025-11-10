
package com.gempukku.lotro.bots.forge.plan.endstate;

import com.gempukku.lotro.bots.forge.plan.action.ActionToTake;
import com.gempukku.lotro.bots.forge.plan.PlannedBoardState;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class PhaseEndState {
    protected final PlannedBoardState boardState;
    protected final List<ActionToTake> fpActions;
    protected final List<ActionToTake> shadowActions;
    protected double value; // Minimax evaluation value for this end state

    public PhaseEndState(PlannedBoardState boardState, List<ActionToTake> fpActions, List<ActionToTake> shadowActions) {
        this.boardState = new PlannedBoardState(boardState);
        this.fpActions = new ArrayList<>(fpActions);
        this.shadowActions = new ArrayList<>(shadowActions);
        this.value = 0.0; // Default value, will be set by minimax exploration
    }

    public PlannedBoardState getBoardState() {
        return boardState;
    }

    public List<ActionToTake> getFpActions() {
        return fpActions;
    }

    public List<ActionToTake> getShadowActions() {
        return shadowActions;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "FP Actions:\n" +
                fpActions.stream().map(Object::toString).collect(Collectors.joining("\n")) +
                "\nShadow Actions:\n" +
                shadowActions.stream().map(Object::toString).collect(Collectors.joining("\n"));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PhaseEndState that = (PhaseEndState) o;
        return Objects.equals(boardState, that.boardState);
    }

    @Override
    public int hashCode() {
        return boardState.hashCode();
    }
}

