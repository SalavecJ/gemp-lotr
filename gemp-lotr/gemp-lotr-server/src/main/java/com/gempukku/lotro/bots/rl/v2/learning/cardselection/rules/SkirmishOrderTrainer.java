package com.gempukku.lotro.bots.rl.v2.learning.cardselection.rules;

import com.gempukku.lotro.bots.BotService;
import com.gempukku.lotro.bots.rl.v2.ModelRegistryV2;
import com.gempukku.lotro.bots.rl.v2.learning.SavedVector;
import com.gempukku.lotro.bots.rl.v2.learning.cardselection.AbstractCardSelectionTrainer;
import com.gempukku.lotro.bots.rl.v2.state.SkirmishOrderStateExtractor;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.Assignment;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import smile.classification.SoftClassifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class SkirmishOrderTrainer extends AbstractCardSelectionTrainer {

    @Override
    protected String getTextTrigger() {
        return "next skirmish to resolve";
    }

    @Override
    public String getAnswer(LotroGame game, AwaitingDecision decision, String playerName, ModelRegistryV2 modelRegistry) {
        List<String> cardIds = Arrays.stream(decision.getDecisionParameters().get("cardId")).toList();

        if (modelRegistry == null) {
            throw new UnsupportedOperationException("Model not found for " + getName());
        }
        SoftClassifier<double[]> model = modelRegistry.getModel(getName());
        if (model == null) {
            throw new UnsupportedOperationException("Model not found for " + getName());
        }
        double[] stateVector = extractFeatures(game.getGameState(), decision, playerName);
        List<ScoredCard> scoredCards = new ArrayList<>();

        for (String physicalId : cardIds) {
            try {
                for (Assignment assignment : game.getGameState().getAssignments()) {
                    if (assignment.getFellowshipCharacter().getCardId() == Integer.parseInt(physicalId)) {
                        String blueprintId = game.getGameState().getBlueprintId(Integer.parseInt(physicalId));
                        double[] cardVector = BotService.staticLibrary.getLotroCardBlueprint(blueprintId).getFpAssignedCardFeatures(game.getGameState(), Integer.parseInt(physicalId), playerName);
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
        ScoredCard best = scoredCards.getFirst();
        return best.cardId;
    }

    @Override
    public double[] extractFeatures(GameState gameState, AwaitingDecision decision, String playerName) {
        return SkirmishOrderStateExtractor.extractFeatures(gameState, decision, playerName);
    }

    @Override
    public List<SavedVector> toStringVectors(LotroGame game, AwaitingDecision decision, String playerId, String answer) {
        String className = getName();
        double[] state = extractFeatures(game.getGameState(), decision, playerId);

        PhysicalCard fpCard = null;
        for (PhysicalCard physicalCard : game.getGameState().getInPlay()) {
            if (physicalCard.getCardId() == Integer.parseInt(answer)) {
                fpCard = physicalCard;
                break;
            }
        }

        if (fpCard == null) {
            throw new IllegalArgumentException("Could not find chosen card in game state.");
        }

        List<String> notChosenOptions = new ArrayList<>();
        for (String cardId : decision.getDecisionParameters().get("cardId")) {
            if (!answer.equals(cardId)) {
                notChosenOptions.add(cardId);
            }
        }

        List<double[]> notChosenVectors = new ArrayList<>();
        for (String notChosenOption : notChosenOptions) {
            try {
                double[] cardVector = BotService.staticLibrary.getLotroCardBlueprint(getBlueprintId(notChosenOption, game.getGameState())).getFpAssignedCardFeatures(game.getGameState(), Integer.parseInt(notChosenOption), playerId);
                notChosenVectors.add(cardVector);
            } catch (CardNotFoundException ignored) {

            }
        }

        List<SavedVector> tbr = new ArrayList<>();
        try {
            double[] cardVector = BotService.staticLibrary.getLotroCardBlueprint(getBlueprintId(answer, game.getGameState())).getFpAssignedCardFeatures(game.getGameState(), fpCard.getCardId(), playerId);
            tbr.add(new SavedVector(className, state, cardVector, notChosenVectors));
        } catch (CardNotFoundException ignored) {

        }

        return tbr;
    }

    private String getBlueprintId(String physicalId, GameState gameState) throws CardNotFoundException {
        for (PhysicalCard card : gameState.getAllCards()) {
            if (card.getCardId() == Integer.parseInt(physicalId)) {
                return card.getBlueprintId();
            }
        }
        throw new CardNotFoundException("Card not found in game state");
    }
}
