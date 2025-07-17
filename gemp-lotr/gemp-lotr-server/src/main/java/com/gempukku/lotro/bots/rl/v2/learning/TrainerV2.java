package com.gempukku.lotro.bots.rl.v2.learning;

import com.gempukku.lotro.bots.rl.learning.LearningStep;
import com.gempukku.lotro.bots.rl.learning.semanticaction.SemanticAction;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import smile.classification.SoftClassifier;

import java.util.List;

public interface TrainerV2 {
    boolean isStepRelevant(LearningStep step);
    SoftClassifier<double[]> train(List<SavedVector> vectors);

    List<SavedVector> toStringVectors(GameState gameState, SemanticAction action, String playerId, AwaitingDecision decision);

    String getName();

    static boolean equal(TrainerV2 t1, TrainerV2 t2) {
        return t1.getClass().equals(t2.getClass()) || t1.getName().equals(t2.getName());
    }
}
