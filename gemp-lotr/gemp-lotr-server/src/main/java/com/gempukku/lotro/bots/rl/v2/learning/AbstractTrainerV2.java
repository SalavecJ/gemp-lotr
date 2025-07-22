package com.gempukku.lotro.bots.rl.v2.learning;

import com.gempukku.lotro.bots.rl.learning.LabeledPoint;
import com.gempukku.lotro.bots.rl.v2.state.GeneralStateExtractor;
import com.gempukku.lotro.bots.rl.v2.state.StateExtractor;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import smile.classification.LogisticRegression;
import smile.classification.SoftClassifier;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractTrainerV2 implements TrainerV2, StateExtractor {

    public SoftClassifier<double[]> trainWithPoints(List<LabeledPoint> points) {
        double[][] x = points.stream().map(LabeledPoint::x).toArray(double[][]::new);
        int[] y = points.stream().mapToInt(LabeledPoint::y).toArray();
        return LogisticRegression.fit(x, y);
    }


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

    protected double[] append(double[] first, double[] second) {
        double[] result = new double[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    @Override
    public SoftClassifier<double[]> train(List<SavedVector> vectors) {
        return trainWithPoints(extractTrainingData(vectors));
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }


    @Override
    public double[] extractFeatures(GameState gameState, AwaitingDecision decision, String playerName) {
        return GeneralStateExtractor.extractFeatures(gameState, decision, playerName);
    }
}
