package com.gempukku.lotro.bots.rl.v2.learning.choice.rules;

import com.gempukku.lotro.bots.rl.v2.learning.choice.AbstractChoiceTrainer;
import com.gempukku.lotro.bots.rl.v2.state.MulliganStateExtractor;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

public class MulliganTrainer extends AbstractChoiceTrainer {
    @Override
    protected String getTextTrigger() {
        return "mulligan";
    }

    @Override
    protected int getNumberOfOptions() {
        return 2;
    }

    @Override
    public double[] extractFeatures(GameState gameState, AwaitingDecision decision, String playerName) {
        return MulliganStateExtractor.extractFeatures(gameState, decision, playerName);
    }
}
