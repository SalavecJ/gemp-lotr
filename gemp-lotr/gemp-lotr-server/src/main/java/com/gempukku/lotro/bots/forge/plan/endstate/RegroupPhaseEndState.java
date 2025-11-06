package com.gempukku.lotro.bots.forge.plan.endstate;

import com.gempukku.lotro.bots.forge.plan.action.ActionToTake;
import com.gempukku.lotro.game.state.PlannedBoardState;

import java.util.List;

public class RegroupPhaseEndState extends PhaseEndState {

    public RegroupPhaseEndState(PlannedBoardState boardState, List<ActionToTake> fpActions, List<ActionToTake> shadowActions) {
        super(boardState, fpActions, shadowActions);
    }
}

