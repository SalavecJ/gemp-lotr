package com.gempukku.lotro.bots.rl.v2.decisions.cardselection.rules;

import com.gempukku.lotro.bots.rl.v2.decisions.cardselection.AbstractCardSelectionAnswerer;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;

public class FpSelfWoundAnswerer extends AbstractCardSelectionAnswerer {
    private static final String ARCHERY = "Choose character to assign archery wound to - remaining wounds:";
    private static final String THREATS = "Threat Rule";

    @Override
    protected String getTextTrigger() {
        return null;
    }

    @Override
    public boolean appliesTo(GameState gameState, AwaitingDecision decision, String playerName) {
        if (decision.getDecisionType() != AwaitingDecisionType.CARD_SELECTION)
            return false;
        return decision.getText().toLowerCase().contains(ARCHERY.toLowerCase()) ||
                decision.getText().toLowerCase().contains(THREATS.toLowerCase());
    }
}
