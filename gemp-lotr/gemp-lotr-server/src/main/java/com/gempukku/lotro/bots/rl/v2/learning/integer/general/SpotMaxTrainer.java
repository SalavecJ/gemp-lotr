package com.gempukku.lotro.bots.rl.v2.learning.integer.general;

import com.gempukku.lotro.bots.rl.v2.ModelRegistryV2;
import com.gempukku.lotro.bots.rl.v2.learning.integer.AbstractIntegerTrainer;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

public class SpotMaxTrainer extends AbstractIntegerTrainer {

    @Override
    protected String getTextTrigger() {
        return "how many to spot";
    }

    @Override
    public String getAnswer(LotroGame game, AwaitingDecision decision, String playerName, ModelRegistryV2 modelRegistry) {
        // Spot as many as possible
        return decision.getDecisionParameters().get("max")[0];
    }
}
