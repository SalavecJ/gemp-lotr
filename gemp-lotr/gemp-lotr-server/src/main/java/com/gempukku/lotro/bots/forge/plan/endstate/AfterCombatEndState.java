package com.gempukku.lotro.bots.forge.plan.endstate;

import com.gempukku.lotro.bots.forge.plan.action2.ActionToTake2;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.List;

public class AfterCombatEndState extends PhaseEndState {
    public AfterCombatEndState(DefaultLotroGame game, List<ActionToTake2> fpActions, List<ActionToTake2> shadowActions) {
        super(game, fpActions, shadowActions);
    }
}
