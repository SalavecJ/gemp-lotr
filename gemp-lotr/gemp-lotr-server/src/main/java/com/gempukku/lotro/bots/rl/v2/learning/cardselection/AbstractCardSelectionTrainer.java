package com.gempukku.lotro.bots.rl.v2.learning.cardselection;

import com.gempukku.lotro.bots.rl.learning.LabeledPoint;
import com.gempukku.lotro.bots.rl.learning.LearningStep;
import com.gempukku.lotro.bots.rl.learning.semanticaction.SemanticAction;
import com.gempukku.lotro.bots.rl.v2.learning.AbstractTrainerV2;
import com.gempukku.lotro.bots.rl.v2.learning.SavedVector;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

import java.util.List;

public class AbstractCardSelectionTrainer extends AbstractTrainerV2 {
    //TODO
    @Override
    protected List<LabeledPoint> extractTrainingData(List<SavedVector> vectors) {
        return null;
    }

    @Override
    public boolean isStepRelevant(LearningStep step) {
        return false;
    }

    @Override
    public List<SavedVector> toStringVectors(GameState gameState, SemanticAction action, String playerId, AwaitingDecision decision) {
        return null;
    }
}
