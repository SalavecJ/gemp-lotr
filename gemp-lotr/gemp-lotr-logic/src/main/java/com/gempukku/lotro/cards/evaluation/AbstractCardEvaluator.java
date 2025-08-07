package com.gempukku.lotro.cards.evaluation;

import com.gempukku.lotro.game.state.LotroGame;

public abstract class AbstractCardEvaluator implements CardEvaluator{
    @Override
    public boolean doesAnythingIfPlayed(LotroGame game, int physicalId, String playerName) {
        throw new UnsupportedOperationException("Not implemented for " + game.getGameState().getBlueprintId(physicalId));
    }

    @Override
    public boolean doesAnythingIfUsed(LotroGame game, int physicalId, String playerName) {
        throw new UnsupportedOperationException("Not implemented for " + game.getGameState().getBlueprintId(physicalId));
    }
}
