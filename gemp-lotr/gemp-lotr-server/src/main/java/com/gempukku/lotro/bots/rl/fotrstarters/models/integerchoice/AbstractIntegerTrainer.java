package com.gempukku.lotro.bots.rl.fotrstarters.models.integerchoice;

import com.gempukku.lotro.bots.rl.learning.LearningStep;
import com.gempukku.lotro.bots.rl.RLGameStateFeatures;
import com.gempukku.lotro.bots.rl.fotrstarters.FotrAbstractTrainer;
import com.gempukku.lotro.bots.rl.learning.LabeledPoint;
import com.gempukku.lotro.bots.rl.ModelRegistry;
import com.gempukku.lotro.bots.rl.learning.semanticaction.IntegerChoiceAction;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import smile.classification.SoftClassifier;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractIntegerTrainer extends FotrAbstractTrainer {

    protected abstract String getTextTrigger(); // e.g., "burdens to bid"
    public abstract int getMaxChoice();

    @Override
    public boolean appliesTo(GameState gameState, AwaitingDecision decision, String playerName) {
        return decision.getDecisionType() == AwaitingDecisionType.INTEGER &&
                decision.getText().toLowerCase().contains(getTextTrigger().toLowerCase());
    }

    @Override
    public String getAnswer(GameState gameState, AwaitingDecision decision, String playerName, RLGameStateFeatures features, ModelRegistry modelRegistry) {
        SoftClassifier<double[]> model = modelRegistry.getModel(getClass());
        double[] stateVector = features.extractFeatures(gameState, decision, playerName);
        double[] probs = new double[getMaxChoice() + 1];
        model.predict(stateVector, probs);
        int predictedValue = sampleFromDistribution(probs);
        return String.valueOf(predictedValue);
    }

    private int sampleFromDistribution(double[] probabilities) {
        double r = Math.random();
        double cumulative = 0.0;
        for (int i = 0; i < probabilities.length; i++) {
            cumulative += probabilities[i];
            if (r < cumulative)
                return i;
        }
        return probabilities.length - 1; // fallback
    }

    protected List<LabeledPoint> extractTrainingData(List<LearningStep> steps) {
        List<LabeledPoint> data = new ArrayList<>();

        for (LearningStep step : steps) {
            if (!isStepRelevant(step)) continue;

            if (step.reward <= 0) continue; // Only learn from positively rewarded steps

            int chosen = ((IntegerChoiceAction) step.action).getValue();
            data.add(new LabeledPoint(chosen, step.state));
        }

        return data;
    }

    @Override
    public boolean isStepRelevant(LearningStep step) {
        return step.decision.getDecisionType() == AwaitingDecisionType.INTEGER &&
                step.decision.getText().toLowerCase().contains(getTextTrigger().toLowerCase()) &&
                step.action instanceof IntegerChoiceAction;
    }
}
