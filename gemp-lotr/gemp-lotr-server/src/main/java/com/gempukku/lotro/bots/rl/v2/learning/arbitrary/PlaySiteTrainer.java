package com.gempukku.lotro.bots.rl.v2.learning.arbitrary;

import com.gempukku.lotro.bots.rl.v2.state.GeneralStateExtractor;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

public class PlaySiteTrainer extends AbstractArbitraryTrainer {
    @Override
    protected String getTextTrigger() {
        return "Choose site to play";
    }

    @Override
    public double[] extractFeatures(GameState gameState, AwaitingDecision decision, String playerName) {
        // TODO something better for shadow sites (count twilight of minions in hand or something like that)
        return GeneralStateExtractor.extractFeatures(gameState, decision, playerName);
    }
}
