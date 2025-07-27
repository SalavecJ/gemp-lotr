package com.gempukku.lotro.bots.rl.v2.learning.cardaction;

import com.gempukku.lotro.bots.BotService;
import com.gempukku.lotro.bots.rl.v2.ModelRegistryV2;
import com.gempukku.lotro.bots.rl.v2.learning.AbstractTrainerV2;
import com.gempukku.lotro.bots.rl.v2.learning.SavedVector;
import com.gempukku.lotro.bots.rl.v2.learning.cardaction.phase.AbstractPhaseCardActionTrainer;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import com.gempukku.lotro.logic.decisions.CardActionSelectionDecision;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import smile.classification.SoftClassifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public abstract class AbstractCardActionTrainer extends AbstractTrainerV2 {

    protected abstract String getTextTrigger();
    protected abstract boolean containsRelevantDecision(GameState gameState, CardActionSelectionDecision decision, String playerName);
    protected abstract boolean relevantCardChosen(GameState gameState, CardActionSelectionDecision decision, String answer);


    @Override
    public boolean appliesTo(GameState gameState, AwaitingDecision decision, String playerName) {
        if (!decision.getDecisionType().equals(AwaitingDecisionType.CARD_ACTION_CHOICE)
                || !(decision instanceof CardActionSelectionDecision cardActionSelectionDecision)) {
            return false;
        }

        if (!decision.getText().contains(getTextTrigger())) {
            return false;
        }

        return containsRelevantDecision(gameState, cardActionSelectionDecision, playerName);
    }

    @Override
    public String getAnswer(GameState gameState, AwaitingDecision decision, String playerName, ModelRegistryV2 modelRegistry) {
        throw new UnsupportedOperationException("Call 'scoreAction' instead on " + getName());
    }

    public void scoreAction(GameState gameState, AwaitingDecision decision, String playerName,
                            ModelRegistryV2 modelRegistry, AbstractPhaseCardActionTrainer.IdActionPair idActionPair) {
        if (!(decision instanceof CardActionSelectionDecision)) {
            return;
        }

        if (modelRegistry == null) {
            throw new UnsupportedOperationException("Model not found for " + getName());
        }
        SoftClassifier<double[]> model = modelRegistry.getModel(getName());
        if (model == null) {
            throw new UnsupportedOperationException("Model not found for " + getName());
        }

        try {
            String blueprintId = gameState.getBlueprintId(Integer.parseInt(idActionPair.cardId));
            double[] cardVector = BotService.staticLibrary.getLotroCardBlueprint(blueprintId).getGeneralCardFeatures(gameState, Integer.parseInt(idActionPair.cardId), playerName);
            double[] stateVector = extractFeatures(gameState, decision, playerName);
            double[] extended = Arrays.copyOf(stateVector, stateVector.length + cardVector.length);
            System.arraycopy(cardVector, 0, extended, stateVector.length, cardVector.length);

            double[] probabilities = new double[2];
            model.predict(extended, probabilities);


            double[] extendedPass = Arrays.copyOf(stateVector, stateVector.length + cardVector.length);
            System.arraycopy(new double[cardVector.length], 0, extended, stateVector.length, cardVector.length);
            double[] probabilitiesPass = new double[2];
            model.predict(extendedPass, probabilitiesPass);


            if (probabilities[1] > probabilitiesPass[1]) {
                idActionPair.confidence = probabilities[1];
            }
        } catch (CardNotFoundException ignored) {

        }
    }

    @Override
    public List<SavedVector> toStringVectors(GameState gameState, AwaitingDecision decision, String playerId, String answer) {
        if (!appliesTo(gameState, decision, playerId)) {
            return List.of();
        }


        Map<String, String[]> params = decision.getDecisionParameters();
        List<String> cardIds = Arrays.stream(params.get("cardId")).toList();
        List<String> blueprintIds = Arrays.stream(params.get("blueprintId")).toList();
        List<String> actionTexts = Arrays.stream(params.get("actionText")).toList();

        double[] chosenVector = new double[17];
        List<double[]> notChosenVectors = new ArrayList<>();


        for (int i = 0; i < actionTexts.size(); i++) {
            try {
                CardActionSelectionDecision tmpDecision = new CardActionSelectionDecision(decision.getAwaitingDecisionId(),
                        decision.getText(),
                        List.of(cardIds.get(i)),
                        List.of(blueprintIds.get(i)),
                        List.of(actionTexts.get(i))) {
                    @Override
                    public void decisionMade(String result) throws DecisionResultInvalidException {

                    }
                };
                if (appliesTo(gameState, tmpDecision, playerId)) {
                    String blueprintId = gameState.getBlueprintId(Integer.parseInt(cardIds.get(i)));
                    double[] cardVector = BotService.staticLibrary.getLotroCardBlueprint(blueprintId).getGeneralCardFeatures(gameState, Integer.parseInt(cardIds.get(i)), playerId);

                    if (String.valueOf(i).equals(answer)) {
                        chosenVector = cardVector;
                    } else {
                        notChosenVectors.add(cardVector);
                    }
                }
            } catch (CardNotFoundException ignored) {

            }
        }

        if (!answer.isEmpty()) {
            notChosenVectors.add(new double[17]);
        }

        if (!(decision instanceof CardActionSelectionDecision cardActionSelectionDecision)) {
            return List.of();
        }

        if (answer.isEmpty() || relevantCardChosen(gameState, cardActionSelectionDecision, answer)) {
            return List.of(new SavedVector(getName(), extractFeatures(gameState, decision, playerId), chosenVector, notChosenVectors));
        }

        return List.of();
    }
}
