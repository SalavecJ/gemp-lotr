package com.gempukku.lotro.bots.rl.v2.decisions.cardselection.rules;

import com.gempukku.lotro.bots.rl.v2.decisions.cardselection.AbstractCardSelectionAnswerer;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;

public class FpArcherySelfWoundAnswerer extends AbstractCardSelectionAnswerer {

    @Override
    protected String getTextTrigger() {
        return "Choose character to assign archery wound to - remaining wounds:";
    }
}
