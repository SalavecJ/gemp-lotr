package com.gempukku.lotro.bots.rl.v2.learning.integer;

import com.gempukku.lotro.bots.rl.v2.learning.AbstractTrainerV2;
import com.gempukku.lotro.bots.rl.v2.learning.SavedVector;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import smile.classification.SoftClassifier;

import java.util.List;

public abstract class AbstractIntegerTrainer extends AbstractTrainerV2 {

    protected abstract String getTextTrigger(); // e.g., "burdens to bid"

    @Override
    public boolean appliesTo(GameState gameState, AwaitingDecision decision, String playerName) {
        return decision.getDecisionType() == AwaitingDecisionType.INTEGER &&
                decision.getText().toLowerCase().contains(getTextTrigger().toLowerCase());
    }

    @Override
    public List<SavedVector> toStringVectors(LotroGame game, AwaitingDecision decision, String playerId, String answer) {
        return List.of(); // No model for integer trainers yet
    }

    @Override
    public SoftClassifier<double[]> train(List<SavedVector> vectors) {
        return null; // No model for integer trainers yet
    }

    @Override
    public double[] extractFeatures(GameState gameState, AwaitingDecision decision, String playerName) {
        return new double[0]; // No model for integer trainers yet
    }
}
