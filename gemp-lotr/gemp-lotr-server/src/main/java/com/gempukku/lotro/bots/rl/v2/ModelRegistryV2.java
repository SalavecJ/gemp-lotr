package com.gempukku.lotro.bots.rl.v2;

import com.gempukku.lotro.bots.rl.v2.decisions.DecisionAnswererV2;
import smile.classification.SoftClassifier;

import java.util.HashMap;
import java.util.Map;

public class ModelRegistryV2 {
    private final Map<Class<? extends DecisionAnswererV2>, SoftClassifier<double[]>> modelMap = new HashMap<>();

    public <T extends DecisionAnswererV2> void registerModel(Class<T> answererClass, SoftClassifier<double[]> model) {
        modelMap.put(answererClass, model);
    }

    public <T extends DecisionAnswererV2> SoftClassifier<double[]> getModel(Class<T> answererClass) {
        return modelMap.get(answererClass);
    }
}
