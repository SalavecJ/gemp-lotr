package com.gempukku.lotro.bots.rl.v2.learning.cardselection;

import com.gempukku.lotro.bots.BotService;
import com.gempukku.lotro.bots.rl.learning.LabeledPoint;
import com.gempukku.lotro.bots.rl.learning.LearningStep;
import com.gempukku.lotro.bots.rl.learning.semanticaction.CardSelectionAction;
import com.gempukku.lotro.bots.rl.learning.semanticaction.SemanticAction;
import com.gempukku.lotro.bots.rl.v2.learning.AbstractTrainerV2;
import com.gempukku.lotro.bots.rl.v2.learning.SavedVector;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractCardSelectionTrainer extends AbstractTrainerV2 {

    protected abstract String getTextTrigger();

    @Override
    protected List<LabeledPoint> extractTrainingData(List<SavedVector> vectors) {
        List<LabeledPoint> data = new ArrayList<>();

        for (SavedVector vector : vectors) {
            if (!vector.className.equals(getName()))
                continue;

            int label = vector.reward > 0 ? 1 : 0;
            data.add(new LabeledPoint(label, append(vector.state, vector.chosen)));
            for (double[] notChosen : vector.notChosen) {
                data.add(new LabeledPoint(1 - label, append(vector.state, notChosen)));
            }
        }

        return data;
    }

    @Override
    public boolean isStepRelevant(LearningStep step) {
        if (step.decision.getDecisionType() != AwaitingDecisionType.CARD_SELECTION)
            return false;

        if (!(step.action instanceof CardSelectionAction))
            return false;


        int min = Integer.parseInt(step.decision.getDecisionParameters().get("min")[0]);
        int max = Integer.parseInt(step.decision.getDecisionParameters().get("max")[0]);
        List<String> cardIds = Arrays.stream(step.decision.getDecisionParameters().get("cardId")).toList();
        if (min == max && min == cardIds.size()) {
            // Had to choose all, nothing to learn from
            return false;
        }

        return step.decision.getText().toLowerCase().contains(getTextTrigger().toLowerCase());
    }

    @Override
    public List<SavedVector> toStringVectors(GameState gameState, SemanticAction action, String playerId, AwaitingDecision decision) {
        String className = getName();
        double[] state = extractFeatures(gameState, decision, playerId);

        List<String> chosenOptions = ((CardSelectionAction) action).getChosenBlueprintIds();
        List<String> notChosenOptions = ((CardSelectionAction) action).getNotChosenBlueprintIds();
        List<Integer> woundsOnChosen = ((CardSelectionAction) action).getWoundsOnChosen();
        List<Integer> woundsOnNotChosen = ((CardSelectionAction) action).getWoundsOnNotChosen();

        List<double[]> notChosenVectors = new ArrayList<>();
        for (int i = 0; i < notChosenOptions.size(); i++) {
            String notChosenOption = notChosenOptions.get(i);
            try {
                double[] cardVector = BotService.staticLibrary.getLotroCardBlueprint(notChosenOption).getGeneralCardFeatures(gameState, -1, playerId, woundsOnNotChosen.get(i));
                notChosenVectors.add(cardVector);
            } catch (CardNotFoundException ignored) {

            }
        }

        List<SavedVector> tbr = new ArrayList<>();
        for (int i = 0; i < chosenOptions.size(); i++) {
            String chosenOption = chosenOptions.get(i);
            try {
                double[] cardVector = BotService.staticLibrary.getLotroCardBlueprint(chosenOption).getGeneralCardFeatures(gameState, -1, playerId, woundsOnChosen.get(i));
                tbr.add(new SavedVector(className, state, cardVector, notChosenVectors));
            } catch (CardNotFoundException ignored) {

            }

        }

        return tbr;
    }

    protected double[] append(double[] first, double[] second) {
        double[] result = new double[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
