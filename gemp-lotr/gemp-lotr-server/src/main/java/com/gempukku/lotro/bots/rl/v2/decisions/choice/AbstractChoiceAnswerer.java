package com.gempukku.lotro.bots.rl.v2.decisions.choice;

import com.gempukku.lotro.bots.rl.ModelRegistry;
import com.gempukku.lotro.bots.rl.RLGameStateFeatures;
import com.gempukku.lotro.bots.rl.learning.semanticaction.MultipleChoiceAction;
import com.gempukku.lotro.bots.rl.v2.ModelRegistryV2;
import com.gempukku.lotro.bots.rl.v2.decisions.AbstractAnswererV2;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import smile.classification.SoftClassifier;

public abstract class AbstractChoiceAnswerer extends AbstractAnswererV2 {

    protected abstract String getTextTrigger();         // e.g. "mulligan", "another move"
    protected abstract String getPositiveOption();      // e.g. "Yes", "Go first"
    protected abstract String getNegativeOption();      // e.g. "No", "Go second"

    @Override
    public boolean appliesTo(GameState gameState, AwaitingDecision decision, String playerName) {
        if (decision.getDecisionType() != AwaitingDecisionType.MULTIPLE_CHOICE)
            return false;

        return decision.getText().toLowerCase().contains(getTextTrigger().toLowerCase());
    }

    @Override
    public String getAnswer(GameState gameState, AwaitingDecision decision, String playerName, ModelRegistryV2 modelRegistry) {
        if (modelRegistry == null) {
            throw new UnsupportedOperationException("Model not found for " + getClass().getSimpleName());
        }
        SoftClassifier<double[]> model = modelRegistry.getModel(getClass());
        if (model == null) {
            throw new UnsupportedOperationException("Model not found for " + getClass().getSimpleName());
        }
        double[] stateVector = extractFeatures(gameState, decision, playerName);
        double[] probs = new double[2];
        model.predict(stateVector, probs);
        return probs[1] > probs[0] ? new MultipleChoiceAction(getPositiveOption()).toDecisionString(decision, gameState) : new MultipleChoiceAction(getNegativeOption()).toDecisionString(decision, gameState);
    }
}
