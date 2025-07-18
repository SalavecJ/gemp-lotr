package com.gempukku.lotro.bots.rl.v2.learning.arbitrary;

import com.gempukku.lotro.bots.BotService;
import com.gempukku.lotro.bots.rl.learning.LabeledPoint;
import com.gempukku.lotro.bots.rl.learning.LearningStep;
import com.gempukku.lotro.bots.rl.learning.semanticaction.ChooseFromArbitraryCardsAction;
import com.gempukku.lotro.bots.rl.learning.semanticaction.SemanticAction;
import com.gempukku.lotro.bots.rl.v2.learning.AbstractTrainerV2;
import com.gempukku.lotro.bots.rl.v2.learning.SavedVector;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractArbitraryTrainer extends AbstractTrainerV2 {
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
        if (step.decision.getDecisionType() != AwaitingDecisionType.ARBITRARY_CARDS)
            return false;

        if (!(step.action instanceof ChooseFromArbitraryCardsAction))
            return false;

        return step.decision.getText().toLowerCase().contains(getTextTrigger().toLowerCase());
    }

    @Override
    public List<SavedVector> toStringVectors(GameState gameState, SemanticAction action, String playerId, AwaitingDecision decision) {
        String className = getName();
        double[] state = extractFeatures(gameState, decision, playerId);

        List<String> chosenOptions = ((ChooseFromArbitraryCardsAction) action).getChosenBlueprintIds();
        List<String> notChosenOptions = ((ChooseFromArbitraryCardsAction) action).getNotChosenBlueprintIds();
        List<double[]> notChosenVectors = new ArrayList<>();
        for (String notChosenOption : notChosenOptions) {
            try {
                double[] cardVector = BotService.staticLibrary.getLotroCardBlueprint(notChosenOption).getGeneralCardFeatures(gameState, -1, playerId);
                notChosenVectors.add(cardVector);
            } catch (CardNotFoundException ignored) {

            }
        }

        List<SavedVector> tbr = new ArrayList<>();

        for (String chosenOption : chosenOptions) {
            try {
                double[] cardVector = BotService.staticLibrary.getLotroCardBlueprint(chosenOption).getGeneralCardFeatures(gameState, -1, playerId);
                tbr.add(new SavedVector(className, state, cardVector, notChosenVectors));
            } catch (CardNotFoundException ignored) {

            }
        }

        return tbr;
    }

    private double[] append(double[] first, double[] second) {
        double[] result = new double[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
