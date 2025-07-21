package com.gempukku.lotro.bots.rl.v2.decisions.choice.rules;

import com.gempukku.lotro.bots.rl.v2.decisions.choice.AbstractChoiceAnswerer;
import com.gempukku.lotro.bots.rl.v2.state.MulliganStateExtractor;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

import java.util.List;

public class MulliganAnswerer extends AbstractChoiceAnswerer {
    @Override
    protected String getTextTrigger() {
        return "mulligan";
    }

    @Override
    protected List<String> getOptions() {
        return List.of("Yes", "No");
    }

    @Override
    public double[] extractFeatures(GameState gameState, AwaitingDecision decision, String playerName) {
        return MulliganStateExtractor.extractFeatures(gameState, decision, playerName);
    }
}
