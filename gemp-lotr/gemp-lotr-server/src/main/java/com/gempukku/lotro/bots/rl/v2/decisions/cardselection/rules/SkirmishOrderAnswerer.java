package com.gempukku.lotro.bots.rl.v2.decisions.cardselection.rules;

import com.gempukku.lotro.bots.BotService;
import com.gempukku.lotro.bots.rl.v2.ModelRegistryV2;
import com.gempukku.lotro.bots.rl.v2.decisions.cardselection.AbstractCardSelectionAnswerer;
import com.gempukku.lotro.bots.rl.v2.state.SkirmishOrderStateExtractor;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.state.Assignment;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import smile.classification.SoftClassifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class SkirmishOrderAnswerer extends AbstractCardSelectionAnswerer {
    @Override
    protected String getTextTrigger() {
        return  "next skirmish to resolve";
    }

    @Override
    public double[] extractFeatures(GameState gameState, AwaitingDecision decision, String playerName) {
        return SkirmishOrderStateExtractor.extractFeatures(gameState, decision, playerName);
    }

    @Override
    public String getAnswer(GameState gameState, AwaitingDecision decision, String playerName, ModelRegistryV2 modelRegistry) {
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
                for (Assignment assignment : gameState.getAssignments()) {
                    if (assignment.getFellowshipCharacter().getCardId() == Integer.parseInt(physicalId)) {
                        String blueprintId = gameState.getBlueprintId(Integer.parseInt(physicalId));
                        double[] cardVector = BotService.staticLibrary.getLotroCardBlueprint(blueprintId).getFpAssignedCardFeatures(gameState, Integer.parseInt(physicalId), playerName);
                        double[] extended = Arrays.copyOf(stateVector, stateVector.length + cardVector.length);
                        System.arraycopy(cardVector, 0, extended, stateVector.length, cardVector.length);

                        double[] probs = new double[2];
                        model.predict(extended, probs);
                        scoredCards.add(new ScoredCard(physicalId, probs[1])); // probability of choosing this card
                    }
                }

            } catch (CardNotFoundException ignored) {

            }
        }

        // Find the card with the highest probability
        scoredCards.sort(Comparator.comparingDouble(c -> -c.score));
        ScoredCard best = scoredCards.get(0);
        return best.cardId;
    }
}
