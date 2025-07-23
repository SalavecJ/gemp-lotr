package com.gempukku.lotro.bots.rl.v2.learning.choice;

import com.gempukku.lotro.bots.rl.learning.LabeledPoint;
import com.gempukku.lotro.bots.rl.v2.learning.AbstractTrainerV2;
import com.gempukku.lotro.bots.rl.v2.learning.SavedVector;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractChoiceTrainer extends AbstractTrainerV2 {
    protected abstract String getTextTrigger();         // e.g. "mulligan", "another move"
    protected abstract int getNumberOfOptions();

    @Override
    public boolean isDecisionRelevant(GameState gameState, AwaitingDecision decision, String playerName) {
        if (decision.getDecisionType() != AwaitingDecisionType.MULTIPLE_CHOICE)
            return false;

        return decision.getText().toLowerCase().contains(getTextTrigger().toLowerCase());
    }

    @Override
    protected List<LabeledPoint> extractTrainingData(List<SavedVector> vectors) {
        List<LabeledPoint> data = new ArrayList<>();

        for (SavedVector vector : vectors) {
            if (!vector.className.equals(getName()))
                continue;

            int chosenIndex = findMatchingIndex(vector.chosen, getNumberOfOptions());
            if (chosenIndex == -1)
                continue;

            if (vector.reward > 0) {
                data.add(new LabeledPoint(chosenIndex, vector.state));
            } else {
                for (double[] alt : vector.notChosen) {
                    int idx = findMatchingIndex(alt, getNumberOfOptions());
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
    public List<SavedVector> toStringVectors(GameState gameState, AwaitingDecision decision, String playerId, String answer) {
        String className = getName();
        double[] state = extractFeatures(gameState, decision, playerId);

        int chosenIndex = Integer.parseInt(answer);

        double[] chosen = new double[getNumberOfOptions()];
        chosen[chosenIndex] = 1.0;

        List<double[]> notChosen = new ArrayList<>();
        for (int i = 0; i < getNumberOfOptions(); i++) {
            if (i != chosenIndex) {
                double[] notChosenVec = new double[getNumberOfOptions()];
                notChosenVec[i] = 1.0;
                notChosen.add(notChosenVec);
            }
        }

        return List.of(new SavedVector(className, state, chosen, notChosen));
    }
}
