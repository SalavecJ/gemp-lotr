package com.gempukku.lotro.bots.rl.v2.learning.choice;


import com.gempukku.lotro.bots.rl.v2.state.GeneralStateExtractor;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

public class AnotherMoveTrainer extends AbstractChoiceTrainer {
    @Override
    protected String getTextTrigger() {
        return "another move";
    }

    @Override
    protected String getPositiveOption() {
        return "Yes";
    }

    @Override
    protected String getNegativeOption() {
        return "No";
    }

    @Override
    public double[] extractFeatures(GameState gameState, AwaitingDecision decision, String playerName) {
        return GeneralStateExtractor.extractFeatures(gameState, decision, playerName);
    }
}
