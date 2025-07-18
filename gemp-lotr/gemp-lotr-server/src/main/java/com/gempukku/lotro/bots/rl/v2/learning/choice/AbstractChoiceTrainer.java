package com.gempukku.lotro.bots.rl.v2.learning.choice;

import com.gempukku.lotro.bots.rl.learning.LabeledPoint;
import com.gempukku.lotro.bots.rl.learning.LearningStep;
import com.gempukku.lotro.bots.rl.learning.semanticaction.MultipleChoiceAction;
import com.gempukku.lotro.bots.rl.learning.semanticaction.SemanticAction;
import com.gempukku.lotro.bots.rl.v2.learning.AbstractTrainerV2;
import com.gempukku.lotro.bots.rl.v2.learning.SavedVector;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractChoiceTrainer extends AbstractTrainerV2 {
    protected abstract String getTextTrigger();         // e.g. "mulligan", "another move"
    protected abstract List<String> getOptions();

    @Override
    public boolean isStepRelevant(LearningStep step) {
        if (step.decision.getDecisionType() != AwaitingDecisionType.MULTIPLE_CHOICE)
            return false;
        if (!(step.action instanceof MultipleChoiceAction))
            return false;

        return step.decision.getText().toLowerCase().contains(getTextTrigger().toLowerCase());
    }

    @Override
    protected List<LabeledPoint> extractTrainingData(List<SavedVector> vectors) {
        List<LabeledPoint> data = new ArrayList<>();
        List<String> options = getOptions();

        for (SavedVector vector : vectors) {
            if (!vector.className.equals(getName()))
                continue;

            int chosenIndex = findMatchingIndex(vector.chosen, options.size());
            if (chosenIndex == -1)
                continue;

            if (vector.reward > 0) {
                data.add(new LabeledPoint(chosenIndex, vector.state));
            } else {
                for (double[] alt : vector.notChosen) {
                    int idx = findMatchingIndex(alt, options.size());
                    if (idx >= 0)
                        data.add(new LabeledPoint(idx, vector.state));
                }
            }
        }

        return data;
    }

    // Helper to find the index of the "1.0" one-hot vector among options
    private int findMatchingIndex(double[] chosen, int totalOptions) {
        if (chosen.length != totalOptions)
            return -1;

        for (int i = 0; i < totalOptions; i++) {
            if (chosen[i] == 1.0)
                return i;
        }
        return -1;
    }

    @Override
    public List<SavedVector> toStringVectors(GameState gameState, SemanticAction action, String playerId, AwaitingDecision decision) {
        String className = getClass().getSimpleName();
        double[] state = extractFeatures(gameState, decision, playerId);

        List<String> options = getOptions();
        String chosenOption = ((MultipleChoiceAction) action).getChosenOption();

        int chosenIndex = options.indexOf(chosenOption);
        if (chosenIndex == -1)
            throw new IllegalArgumentException("Chosen option not found in options");

        double[] chosen = new double[options.size()];
        chosen[chosenIndex] = 1.0;

        List<double[]> notChosen = new ArrayList<>();
        for (int i = 0; i < options.size(); i++) {
            if (i != chosenIndex) {
                double[] notChosenVec = new double[options.size()];
                notChosenVec[i] = 1.0;
                notChosen.add(notChosenVec);
            }
        }

        return List.of(new SavedVector(className, state, chosen, notChosen));
    }
}
