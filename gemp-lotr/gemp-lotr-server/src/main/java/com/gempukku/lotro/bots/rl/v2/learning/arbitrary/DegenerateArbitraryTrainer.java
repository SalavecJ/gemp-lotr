package com.gempukku.lotro.bots.rl.v2.learning.arbitrary;

import com.gempukku.lotro.bots.rl.learning.LabeledPoint;
import com.gempukku.lotro.bots.rl.v2.ModelRegistryV2;
import com.gempukku.lotro.bots.rl.v2.learning.SavedVector;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import smile.classification.SoftClassifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DegenerateArbitraryTrainer extends AbstractArbitraryTrainer {
    @Override
    public boolean appliesTo(GameState gameState, AwaitingDecision decision, String playerName) {
        if (decision.getDecisionType() != AwaitingDecisionType.ARBITRARY_CARDS)
            return false;

        Map<String, String[]> params = decision.getDecisionParameters();
        String[] cardIds = params.get("cardId");
        String[] selectable = params.get("selectable");
        int min = params.containsKey("min") ? Integer.parseInt(params.get("min")[0]) : 0;
        int max = params.containsKey("max") ? Integer.parseInt(params.get("max")[0]) : cardIds.length;

        // Check if something is selectable
        if (cardIds == null || selectable == null || cardIds.length != selectable.length)
            return true; // Invalid input
        // Collect all selectable indices
        List<Integer> selectableIndices = new ArrayList<>();
        for (int i = 0; i < selectable.length; i++) {
            if (Boolean.parseBoolean(selectable[i])) {
                selectableIndices.add(i);
            }
        }
        // If nothing is selectable or max == 0, the decision is just pass; or we need to select all
        return selectableIndices.isEmpty() || max == 0 || (min == max && max == selectableIndices.size());
    }

    @Override
    public String getAnswer(LotroGame game, AwaitingDecision decision, String playerName, ModelRegistryV2 modelRegistry) {Map<String, String[]> params = decision.getDecisionParameters();
        String[] cardIds = params.get("cardId");
        String[] selectable = params.get("selectable");
        int min = params.containsKey("min") ? Integer.parseInt(params.get("min")[0]) : 0;
        int max = params.containsKey("max") ? Integer.parseInt(params.get("max")[0]) : cardIds.length;

        // Check if something is selectable
        if (cardIds == null || selectable == null || cardIds.length != selectable.length)
            return ""; // Invalid input
        // Collect all selectable indices
        List<Integer> selectableIndices = new ArrayList<>();
        for (int i = 0; i < selectable.length; i++) {
            if (Boolean.parseBoolean(selectable[i])) {
                selectableIndices.add(i);
            }
        }
        if (selectableIndices.isEmpty() || max == 0)
            return ""; // No decision to be made, pass

        if (min == max && max == selectableIndices.size()) {
            // Select all
            List<String> tbr = new ArrayList<>();
            for (Integer selectableIndex : selectableIndices) {
                tbr.add(cardIds[selectableIndex]);
            }
            return String.join(",", tbr);
        }

        // Should not happen, pass
        return "";
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
    public List<SavedVector> toStringVectors(LotroGame game, AwaitingDecision decision, String playerId, String answer) {
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
