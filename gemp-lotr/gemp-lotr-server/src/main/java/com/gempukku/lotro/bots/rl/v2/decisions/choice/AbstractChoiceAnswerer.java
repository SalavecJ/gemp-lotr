package com.gempukku.lotro.bots.rl.v2.decisions.choice;

import com.gempukku.lotro.bots.rl.learning.semanticaction.MultipleChoiceAction;
import com.gempukku.lotro.bots.rl.v2.ModelRegistryV2;
import com.gempukku.lotro.bots.rl.v2.decisions.AbstractAnswererV2;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import smile.classification.SoftClassifier;

import java.util.List;

public abstract class AbstractChoiceAnswerer extends AbstractAnswererV2 {

    protected abstract String getTextTrigger();         // e.g. "mulligan", "another move"
    protected abstract List<String> getOptions();

    @Override
    public boolean appliesTo(GameState gameState, AwaitingDecision decision, String playerName) {
        if (decision.getDecisionType() != AwaitingDecisionType.MULTIPLE_CHOICE)
            return false;

        return decision.getText().toLowerCase().contains(getTextTrigger().toLowerCase());
    }

    @Override
    public String getAnswer(GameState gameState, AwaitingDecision decision, String playerName, ModelRegistryV2 modelRegistry) {
        if (modelRegistry == null) {
            throw new UnsupportedOperationException("Model not found for " + getName());
        }
        SoftClassifier<double[]> model = modelRegistry.getModel(getClass());
        if (model == null) {
            model = modelRegistry.getModel(getName());
        }
        if (model == null) {
            throw new UnsupportedOperationException("Model not found for " + getName());
        }
        double[] features = extractFeatures(gameState, decision, playerName);
        double[] probabilities = new double[getOptions().size()];
        model.predict(features, probabilities);

        // Find highest probability option
        int bestIndex = 0;
        for (int i = 1; i < probabilities.length; i++) {
            if (probabilities[i] > probabilities[bestIndex]) {
                bestIndex = i;
            }
        }

        return new MultipleChoiceAction(getOptions().get(bestIndex)).toDecisionString(decision, gameState);
    }
}
