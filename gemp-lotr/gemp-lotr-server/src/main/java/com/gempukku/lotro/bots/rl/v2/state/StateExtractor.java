package com.gempukku.lotro.bots.rl.v2.state;

import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

public interface StateExtractor {
    double[] extractFeatures(GameState gameState, AwaitingDecision decision, String playerName);
}
