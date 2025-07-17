package com.gempukku.lotro.bots.rl.v2.decisions;

import com.gempukku.lotro.bots.rl.v2.state.GeneralStateExtractor;
import com.gempukku.lotro.bots.rl.v2.state.StateExtractor;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

public abstract class AbstractAnswererV2 implements DecisionAnswererV2, StateExtractor {
    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public double[] extractFeatures(GameState gameState, AwaitingDecision decision, String playerName) {
        return GeneralStateExtractor.extractFeatures(gameState, decision, playerName);
    }
}
