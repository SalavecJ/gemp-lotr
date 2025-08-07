package com.gempukku.lotro.cards.evaluation;

import com.gempukku.lotro.game.state.LotroGame;

public interface CardEvaluator {
    boolean doesAnythingIfPlayed(LotroGame game, int physicalId, String playerName);
    boolean doesAnythingIfUsed(LotroGame game, int physicalId, String playerName);
}
