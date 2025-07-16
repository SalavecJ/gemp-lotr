package com.gempukku.lotro.bots.rl.v2.learning;

import com.gempukku.lotro.bots.rl.v2.ModelRegistryV2;
import com.gempukku.lotro.bots.rl.v2.decisions.DecisionAnswererV2;
import com.gempukku.lotro.bots.rl.v2.decisions.choice.AnotherMoveAnswerer;
import com.gempukku.lotro.bots.rl.v2.learning.choice.AnotherMoveTrainer;
import org.apache.commons.collections4.list.UnmodifiableList;
import smile.classification.SoftClassifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrainersV2 {
    private static final Map<Class<? extends TrainerV2>, Class<? extends DecisionAnswererV2>> map = new HashMap<>();

    private static final List<Class<? extends TrainerV2>> trainerClasses = new ArrayList<>();

    private static final List<TrainerV2> trainers;

    static {
        map.put(AnotherMoveTrainer.class, AnotherMoveAnswerer.class);

        trainerClasses.addAll(map.keySet());

        trainers = new ArrayList<>();
        for (Class<? extends TrainerV2> trainerClass : trainerClasses) {
            try {
                trainers.add(trainerClass.getDeclaredConstructor().newInstance());
            } catch (Exception e) {
                throw new RuntimeException("Failed to instantiate trainer: " + trainerClass.getName(), e);
            }
        }
    }

    private TrainersV2() {

    }

    public static List<TrainerV2> getAllV2Trainers() {
        return new UnmodifiableList<>(trainers);
    }

    public static List<Class<? extends TrainerV2>> getAllV2TrainerClasses() {
        return new UnmodifiableList<>(trainerClasses);
    }

    public static void trainModels(ModelRegistryV2 modelRegistry) {
        for (Map.Entry<Class<? extends TrainerV2>, Class<? extends DecisionAnswererV2>> trainerAnswererEntry : map.entrySet()) {
            try {
                TrainerV2 trainer = trainerAnswererEntry.getKey().getDeclaredConstructor().newInstance();
                List<SavedVector> vectors = SavedVectorPersistence.load(trainer);
                SoftClassifier<double[]> model = trainer.train(vectors);
                modelRegistry.registerModel(trainerAnswererEntry.getValue(), model);
            } catch (Exception e) {
                throw new RuntimeException("Failed to make model for trainer: " + trainerAnswererEntry.getKey().getSimpleName(), e);
            }
        }
    }
}
