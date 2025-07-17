package com.gempukku.lotro.bots.rl.v2.decisions.arbitrary;

import com.gempukku.lotro.bots.BotService;
import com.gempukku.lotro.bots.rl.DecisionAnswerer;
import com.gempukku.lotro.bots.rl.v2.ModelRegistryV2;
import com.gempukku.lotro.bots.rl.v2.decisions.AbstractAnswererV2;
import com.gempukku.lotro.bots.rl.v2.state.GeneralStateExtractor;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import smile.classification.SoftClassifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public abstract class AbstractArbitraryAnswerer extends AbstractAnswererV2 {

    protected abstract String getTextTrigger();

    @Override
    public boolean appliesTo(GameState gameState, AwaitingDecision decision, String playerName) {
        if (decision.getDecisionType() != AwaitingDecisionType.ARBITRARY_CARDS)
            return false;
        return decision.getText().toLowerCase().contains(getTextTrigger().toLowerCase());
    }

    @Override
    public String getAnswer(GameState gameState, AwaitingDecision decision, String playerName, ModelRegistryV2 modelRegistry) {
        int max = Integer.parseInt(decision.getDecisionParameters().get("max")[0]);
        List<String> selectableIds = new ArrayList<>();
        List<String> selectableBlueprints = new ArrayList<>();
        List<String> cardIds = Arrays.stream(decision.getDecisionParameters().get("cardId")).toList();
        List<String> blueprints = Arrays.stream(decision.getDecisionParameters().get("blueprintId")).toList();
        List<String> selectable = Arrays.stream(decision.getDecisionParameters().get("selectable")).toList();

        for (int i = 0; i < cardIds.size() && i < selectable.size(); i++) {
            if (Boolean.parseBoolean(selectable.get(i))) {
                selectableIds.add(cardIds.get(i));
                selectableBlueprints.add(blueprints.get(i));
            }
        }

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
        List<DecisionAnswerer.ScoredCard> scoredCards = new ArrayList<>();

        for (int i = 0; i < selectableBlueprints.size(); i++) {
            try {
                String blueprintId = selectableBlueprints.get(i);
                double[] cardVector = BotService.staticLibrary.getLotroCardBlueprint(blueprintId).getGeneralCardFeatures(gameState, -1, playerName);
                double[] extended = Arrays.copyOf(stateVector, stateVector.length + cardVector.length);
                System.arraycopy(cardVector, 0, extended, stateVector.length, cardVector.length);

                double[] probs = new double[2];
                model.predict(extended, probs);
                scoredCards.add(new DecisionAnswerer.ScoredCard(selectableIds.get(i), probs[1]));
            } catch (CardNotFoundException ignored) {

            }
        }

        scoredCards.sort(Comparator.comparingDouble(c -> -c.score));
        List<String> sortedIds = new ArrayList<>();
        scoredCards.forEach(scoredCard -> sortedIds.add(scoredCard.cardId));
        // Always chooses max cards, never passes
        return String.join(",", sortedIds.subList(0, max));
    }

    @Override
    public double[] extractFeatures(GameState gameState, AwaitingDecision decision, String playerName) {
        return GeneralStateExtractor.extractFeatures(gameState, decision, playerName);
    }
}
