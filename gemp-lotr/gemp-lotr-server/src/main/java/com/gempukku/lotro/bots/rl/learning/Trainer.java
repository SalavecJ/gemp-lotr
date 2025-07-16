package com.gempukku.lotro.bots.rl.learning;

import com.gempukku.lotro.bots.rl.learning.semanticaction.SemanticAction;
import com.gempukku.lotro.bots.rl.v2.learning.SavedVector;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import smile.classification.SoftClassifier;

import java.util.List;

public interface Trainer {
    boolean isStepRelevant(LearningStep step);
    SoftClassifier<double[]> train(List<LearningStep> steps);

    static boolean equal(Trainer t1, Trainer t2) {
        return t1.getClass().equals(t2.getClass());
    }
}
