package com.gempukku.lotro.bots.forge.plan.endstate;

import com.gempukku.lotro.bots.forge.plan.action.ActionToTake;
import com.gempukku.lotro.bots.forge.plan.PlannedBoardState;

import java.util.List;

public class SkirmishPhaseEndState extends PhaseEndState {

    public SkirmishPhaseEndState(PlannedBoardState boardState, List<ActionToTake> fpActions, List<ActionToTake> shadowActions) {
        super(boardState, fpActions, shadowActions);
    }
}

