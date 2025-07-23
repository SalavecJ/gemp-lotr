package com.gempukku.lotro.bots.rl.v2;

import smile.classification.SoftClassifier;

import java.util.HashMap;
import java.util.Map;

public class ModelRegistryV2 {
    private final Map<String, SoftClassifier<double[]>> nameModelMap = new HashMap<>();

    public void registerModel(String key, SoftClassifier<double[]> model) {
        nameModelMap.put(key, model);
    }

    public SoftClassifier<double[]> getModel(String key) {
        return nameModelMap.get(key);
    }
}
