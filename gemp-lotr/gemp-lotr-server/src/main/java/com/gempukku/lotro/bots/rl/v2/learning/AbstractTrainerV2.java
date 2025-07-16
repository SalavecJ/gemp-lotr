package com.gempukku.lotro.bots.rl.v2.learning;

import com.gempukku.lotro.bots.rl.learning.LabeledPoint;
import com.gempukku.lotro.bots.rl.v2.state.StateExtractor;
import smile.classification.LogisticRegression;
import smile.classification.SoftClassifier;

import java.util.List;

public abstract class AbstractTrainerV2 implements TrainerV2, StateExtractor {

    abstract protected List<LabeledPoint> extractTrainingData(List<SavedVector> vectors);

    public SoftClassifier<double[]> trainWithPoints(List<LabeledPoint> points) {
        double[][] x = points.stream().map(LabeledPoint::x).toArray(double[][]::new);
        int[] y = points.stream().mapToInt(LabeledPoint::y).toArray();
        return LogisticRegression.fit(x, y);
    }

    @Override
    public SoftClassifier<double[]> train(List<SavedVector> vectors) {
        return trainWithPoints(extractTrainingData(vectors));
    }
}
