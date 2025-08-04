package com.gempukku.lotro.cards.evaluation;

import com.gempukku.lotro.game.state.GameState;

public interface CardEvaluator {
    boolean doesAnythingIfPlayed(GameState gameState, int physicalId, String playerName);
}
