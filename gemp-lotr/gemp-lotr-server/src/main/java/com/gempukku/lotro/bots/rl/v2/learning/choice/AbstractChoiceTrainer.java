package com.gempukku.lotro.bots.rl.v2.learning.choice;

import com.gempukku.lotro.bots.rl.learning.LabeledPoint;
import com.gempukku.lotro.bots.rl.learning.LearningStep;
import com.gempukku.lotro.bots.rl.learning.semanticaction.MultipleChoiceAction;
import com.gempukku.lotro.bots.rl.learning.semanticaction.SemanticAction;
import com.gempukku.lotro.bots.rl.v2.learning.AbstractTrainerV2;
import com.gempukku.lotro.bots.rl.v2.learning.SavedVector;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractChoiceTrainer extends AbstractTrainerV2 {
    protected abstract String getTextTrigger();         // e.g. "mulligan", "another move"
    protected abstract String getPositiveOption();      // e.g. "Yes", "Go first"
    protected abstract String getNegativeOption();      // e.g. "No", "Go second"

    @Override
    public boolean isStepRelevant(LearningStep step) {
        if (step.decision.getDecisionType() != AwaitingDecisionType.MULTIPLE_CHOICE)
            return false;
        if (!(step.action instanceof MultipleChoiceAction))
            return false;

        return step.decision.getText().toLowerCase().contains(getTextTrigger().toLowerCase());
    }

    @Override
    protected List<LabeledPoint> extractTrainingData(List<SavedVector> vectors) {
        List<LabeledPoint> data = new ArrayList<>();
        for (SavedVector vector : vectors) {
            if (vector.className.equals(getClass().getSimpleName()) && vector.notChosen.size() == 1) {
                double[] state = vector.state;
                double[] chosen = vector.chosen;

                boolean chosePositive = chosen[0] == 1.0;

                int label = vector.reward > 0 ? (chosePositive ? 1 : 0) : (chosePositive ? 0 : 1);
                data.add(new LabeledPoint(label, state));
            }
        }
        return data;
    }

    @Override
    public SavedVector toStringVector(GameState gameState, SemanticAction action, String playerId, AwaitingDecision decision) {
        String className = getClass().getSimpleName();
        double[] state = extractFeatures(gameState, decision, playerId);
        double[] chosen = {((MultipleChoiceAction) action).getChosenOption().equals(getPositiveOption()) ? 1.0 : 0.0};
        List<double[]> notChosen = List.of(new double[] {((MultipleChoiceAction) action).getChosenOption().equals(getPositiveOption()) ? 0.0 : 1.0});
        return new SavedVector(className, state, chosen, notChosen);
    }
}
