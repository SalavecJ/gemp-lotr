package com.gempukku.lotro.bots.rl.v2.decisions.cardselection;

import com.gempukku.lotro.bots.BotService;
import com.gempukku.lotro.bots.rl.v2.ModelRegistryV2;
import com.gempukku.lotro.bots.rl.v2.decisions.AbstractAnswererV2;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import smile.classification.SoftClassifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public abstract class AbstractCardSelectionAnswerer extends AbstractAnswererV2 {

    protected abstract String getTextTrigger();

    @Override
    public boolean appliesTo(GameState gameState, AwaitingDecision decision, String playerName) {
        if (decision.getDecisionType() != AwaitingDecisionType.CARD_SELECTION)
            return false;
        return decision.getText().toLowerCase().contains(getTextTrigger().toLowerCase());
    }

    @Override
    public String getAnswer(GameState gameState, AwaitingDecision decision, String playerName, ModelRegistryV2 modelRegistry) {
        // Always choose max
        int max = Integer.parseInt(decision.getDecisionParameters().get("max")[0]);
        List<String> cardIds = Arrays.stream(decision.getDecisionParameters().get("cardId")).toList();

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
        double[] stateVector = extractFeatures(gameState, decision, playerName);
        List<ScoredCard> scoredCards = new ArrayList<>();

        for (String physicalId : cardIds) {
            try {
                String blueprintId = gameState.getBlueprintId(Integer.parseInt(physicalId));
                double[] cardVector = BotService.staticLibrary.getLotroCardBlueprint(blueprintId).getGeneralCardFeatures(gameState, Integer.parseInt(physicalId), playerName);
                double[] extended = Arrays.copyOf(stateVector, stateVector.length + cardVector.length);
                System.arraycopy(cardVector, 0, extended, stateVector.length, cardVector.length);

                double[] probs = new double[2];
                model.predict(extended, probs);
                scoredCards.add(new ScoredCard(physicalId, probs[1]));
            } catch (CardNotFoundException ignored) {

            }
        }

        scoredCards.sort(Comparator.comparingDouble(c -> -c.score));
        List<String> sortedIds = new ArrayList<>();
        scoredCards.forEach(scoredCard -> sortedIds.add(scoredCard.cardId));
        return String.join(",", sortedIds.subList(0, max));
    }
}
