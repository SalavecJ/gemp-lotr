package com.gempukku.lotro.bots.rl.v2.learning.arbitrary;

import com.gempukku.lotro.bots.BotService;
import com.gempukku.lotro.bots.rl.v2.ModelRegistryV2;
import com.gempukku.lotro.bots.rl.v2.learning.AbstractTrainerV2;
import com.gempukku.lotro.bots.rl.v2.learning.SavedVector;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import smile.classification.SoftClassifier;

import java.util.*;

public abstract class AbstractArbitraryTrainer extends AbstractTrainerV2 {
    protected abstract String getTextTrigger();

    @Override
    public boolean appliesTo(GameState gameState, AwaitingDecision decision, String playerName) {
        if (decision.getDecisionType() != AwaitingDecisionType.ARBITRARY_CARDS)
            return false;

        Map<String, String[]> params = decision.getDecisionParameters();
        String[] cardIds = params.get("cardId");
        int min = params.containsKey("min") ? Integer.parseInt(params.get("min")[0]) : 0;
        int max = params.containsKey("max") ? Integer.parseInt(params.get("max")[0]) : cardIds.length;
        if ((min == max && min == cardIds.length) || max == 0) {
            // Had to choose all, nothing to learn from
            return false;
        }

        return decision.getText().toLowerCase().contains(getTextTrigger().toLowerCase());
    }

    @Override
    public String getAnswer(LotroGame game, AwaitingDecision decision, String playerName, ModelRegistryV2 modelRegistry) {
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
        SoftClassifier<double[]> model = modelRegistry.getModel(getName());
        if (model == null) {
            throw new UnsupportedOperationException("Model not found for " + getName());
        }
        double[] stateVector = extractFeatures(game.getGameState(), decision, playerName);
        List<ScoredCard> scoredCards = new ArrayList<>();

        for (int i = 0; i < selectableBlueprints.size(); i++) {
            try {
                String blueprintId = selectableBlueprints.get(i);
                double[] cardVector = BotService.staticLibrary.getLotroCardBlueprint(blueprintId).getGeneralCardFeatures(game.getGameState(), -1, playerName);
                double[] extended = Arrays.copyOf(stateVector, stateVector.length + cardVector.length);
                System.arraycopy(cardVector, 0, extended, stateVector.length, cardVector.length);

                double[] probs = new double[2];
                model.predict(extended, probs);
                scoredCards.add(new ScoredCard(selectableIds.get(i), probs[1]));
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
    public List<SavedVector> toStringVectors(LotroGame game, AwaitingDecision decision, String playerId, String answer) {
        String className = getName();
        double[] state = extractFeatures(game.getGameState(), decision, playerId);

        List<String> chosenIds = Arrays.stream(answer.split(",")).toList();
        Map<String, String> chosenOptions = new HashMap<>();
        List<String> blueprintIds = Arrays.stream(decision.getDecisionParameters().get("blueprintId")).toList();
        List<String> cardIds = Arrays.stream(decision.getDecisionParameters().get("cardId")).toList();
        List<String> selectable = Arrays.stream(decision.getDecisionParameters().get("selectable")).toList();

        List<String> selectableIds = new ArrayList<>();
        for (int i = 0; i < cardIds.size() && i < selectable.size(); i++) {
            if (Boolean.parseBoolean(selectable.get(i))) {
                selectableIds.add(cardIds.get(i));
            }
        }

        for (int i = 0; i < cardIds.size(); i++) {
            String cardId = cardIds.get(i);
            if (chosenIds.contains(cardId)) {
                chosenOptions.put(cardId, blueprintIds.get(i));
            }
        }

        Map<String, String> notChosenOptions = new HashMap<>();
        for (int i = 0; i < cardIds.size(); i++) {
            String cardId = cardIds.get(i);
            if (selectableIds.contains(cardId) && !chosenOptions.containsKey(cardId)) {
                notChosenOptions.put(cardId, blueprintIds.get(i));
            }
        }

        List<double[]> notChosenVectors = new ArrayList<>();
        for (Map.Entry<String, String> entry : notChosenOptions.entrySet()) {
            try {
                double[] cardVector = BotService.staticLibrary.getLotroCardBlueprint(entry.getValue()).getGeneralCardFeatures(game.getGameState(), -1, playerId);
                notChosenVectors.add(cardVector);
            } catch (CardNotFoundException ignored) {

            }

        }

        List<SavedVector> tbr = new ArrayList<>();

        for (Map.Entry<String, String> entry : chosenOptions.entrySet()) {
            try {
                double[] cardVector = BotService.staticLibrary.getLotroCardBlueprint(entry.getValue()).getGeneralCardFeatures(game.getGameState(), -1, playerId);
                tbr.add(new SavedVector(className, state, cardVector, notChosenVectors));
            } catch (CardNotFoundException ignored) {

            }
        }

        return tbr;
    }
}
