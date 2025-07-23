package com.gempukku.lotro.bots.rl.v2.learning.cardselection;

import com.gempukku.lotro.bots.rl.learning.LabeledPoint;
import com.gempukku.lotro.bots.rl.v2.ModelRegistryV2;
import com.gempukku.lotro.bots.rl.v2.learning.SavedVector;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import smile.classification.SoftClassifier;

import java.util.Arrays;
import java.util.List;

public class DegenerateCardSelectionTrainer extends AbstractCardSelectionTrainer {
    @Override
    public boolean appliesTo(GameState gameState, AwaitingDecision decision, String playerName) {
        if (decision.getDecisionType() != AwaitingDecisionType.CARD_SELECTION)
            return false;


        int min = Integer.parseInt(decision.getDecisionParameters().get("min")[0]);
        int max = Integer.parseInt(decision.getDecisionParameters().get("max")[0]);
        List<String> cardIds = Arrays.stream(decision.getDecisionParameters().get("cardId")).toList();

        return (min == max && min == cardIds.size()) || max == 0;
    }

    @Override
    public String getAnswer(GameState gameState, AwaitingDecision decision, String playerName, ModelRegistryV2 modelRegistry) {
        // Choose all
        return String.join(",", Arrays.stream(decision.getDecisionParameters().get("cardId")).toList());
    }

    @Override
    protected String getTextTrigger() {
        return null; // Not sed
    }

    @Override
    public double[] extractFeatures(GameState gameState, AwaitingDecision decision, String playerName) {
        return null; // Not used
    }

    @Override
    public List<SavedVector> toStringVectors(GameState gameState, AwaitingDecision decision, String playerId, String answer) {
        return List.of();
    }

    @Override
    public SoftClassifier<double[]> train(List<SavedVector> vectors) {
        return null;
    }

    @Override
    protected List<LabeledPoint> extractTrainingData(List<SavedVector> vectors) {
        return List.of();
    }
}
