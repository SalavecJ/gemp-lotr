package com.gempukku.lotro.bots.rl.v2.decisions.arbitrary;

import com.gempukku.lotro.bots.rl.v2.ModelRegistryV2;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DegenerateArbitraryDecisionAnswerer extends AbstractArbitraryAnswerer {
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
        // If nothing is selectable or max == 0, the decision is just pass
        return selectableIndices.isEmpty() || max == 0;
    }

    @Override
    public String getAnswer(GameState gameState, AwaitingDecision decision, String playerName, ModelRegistryV2 modelRegistry) {
        return ""; // No decision to be made, pass
    }

    @Override
    protected String getTextTrigger() {
        return null; // Not sed
    }

    @Override
    public double[] extractFeatures(GameState gameState, AwaitingDecision decision, String playerName) {
        return null; // Not used
    }
}
