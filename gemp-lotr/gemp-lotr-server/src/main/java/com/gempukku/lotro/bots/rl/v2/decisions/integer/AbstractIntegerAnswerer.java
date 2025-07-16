package com.gempukku.lotro.bots.rl.v2.decisions.integer;

import com.gempukku.lotro.bots.rl.v2.decisions.AbstractAnswererV2;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
public abstract class AbstractIntegerAnswerer extends AbstractAnswererV2 {

    protected abstract String getTextTrigger(); // e.g., "burdens to bid"


    @Override
    public boolean appliesTo(GameState gameState, AwaitingDecision decision, String playerName) {
        return decision.getDecisionType() == AwaitingDecisionType.INTEGER &&
                decision.getText().toLowerCase().contains(getTextTrigger().toLowerCase());
    }
}
