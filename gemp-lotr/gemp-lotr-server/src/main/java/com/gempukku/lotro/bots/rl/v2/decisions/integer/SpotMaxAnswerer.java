package com.gempukku.lotro.bots.rl.v2.decisions.integer;

import com.gempukku.lotro.bots.rl.v2.ModelRegistryV2;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

public class SpotMaxAnswerer extends AbstractIntegerAnswerer {

    @Override
    protected String getTextTrigger() {
        return "how many to spot";
    }

    @Override
    public String getAnswer(GameState gameState, AwaitingDecision decision, String playerName, ModelRegistryV2 modelRegistry) {
        // Spot as many as possible
        return decision.getDecisionParameters().get("max")[0];
    }

    @Override
    public double[] extractFeatures(GameState gameState, AwaitingDecision decision, String playerName) {
        return new double[0];
    }
}
