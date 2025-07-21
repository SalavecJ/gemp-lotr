package com.gempukku.lotro.bots.rl.v2.decisions.arbitrary.rules;

import com.gempukku.lotro.bots.rl.v2.decisions.arbitrary.AbstractArbitraryAnswerer;
import com.gempukku.lotro.bots.rl.v2.state.StartingFellowshipStateExtractor;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

public class StartingFellowshipAnswerer extends AbstractArbitraryAnswerer {
    @Override
    protected String getTextTrigger() {
        return "starting fellowship";
    }

    @Override
    public double[] extractFeatures(GameState gameState, AwaitingDecision decision, String playerName) {
        return StartingFellowshipStateExtractor.extractFeatures(gameState, decision, playerName);
    }
}
