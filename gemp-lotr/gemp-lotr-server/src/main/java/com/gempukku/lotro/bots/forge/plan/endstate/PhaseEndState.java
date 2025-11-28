
package com.gempukku.lotro.bots.forge.plan.endstate;

import com.gempukku.lotro.bots.forge.plan.action2.ActionToTake2;
import com.gempukku.lotro.game.DefaultUserFeedback;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class PhaseEndState {
    protected final DefaultLotroGame copy;
    protected final List<ActionToTake2> fpActions;
    protected final List<ActionToTake2> shadowActions;
    protected double value; // Minimax evaluation value for this end state

    public PhaseEndState(DefaultLotroGame game, List<ActionToTake2> fpActions, List<ActionToTake2> shadowActions) {
        this.copy = game.getCopyByReplayingDecisionsFromLastCheckpoint(new DefaultUserFeedback());
        this.fpActions = new ArrayList<>(fpActions);
        this.shadowActions = new ArrayList<>(shadowActions);
        this.value = 0.0; // Default value, will be set by minimax exploration
    }

    public DefaultLotroGame getGameCopy() {
        return copy;
    }

    public List<ActionToTake2> getFpActions() {
        return fpActions;
    }

    public List<ActionToTake2> getShadowActions() {
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
}

