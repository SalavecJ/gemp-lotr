package com.gempukku.lotro.bots.rl.v2.learning;

import com.gempukku.lotro.bots.rl.v2.ModelRegistryV2;
import com.gempukku.lotro.bots.rl.v2.decisions.DecisionAnswererV2;
import com.gempukku.lotro.bots.rl.v2.decisions.arbitrary.PlaySiteAnswerer;
import com.gempukku.lotro.bots.rl.v2.decisions.arbitrary.StartingFellowshipAnswerer;
import com.gempukku.lotro.bots.rl.v2.decisions.cardselection.general.AttachItemAnswerer;
import com.gempukku.lotro.bots.rl.v2.decisions.cardselection.rules.*;
import com.gempukku.lotro.bots.rl.v2.decisions.choice.rules.AnotherMoveAnswerer;
import com.gempukku.lotro.bots.rl.v2.decisions.choice.rules.MulliganAnswerer;
import com.gempukku.lotro.bots.rl.v2.learning.arbitrary.PlaySiteTrainer;
import com.gempukku.lotro.bots.rl.v2.learning.arbitrary.StartingFellowshipTrainer;
import com.gempukku.lotro.bots.rl.v2.learning.cardselection.general.AttachItemTrainer;
import com.gempukku.lotro.bots.rl.v2.learning.cardselection.rules.*;
import com.gempukku.lotro.bots.rl.v2.learning.choice.rules.AnotherMoveTrainer;
import com.gempukku.lotro.bots.rl.v2.learning.choice.rules.MulliganTrainer;
import org.apache.commons.collections4.list.UnmodifiableList;
import smile.classification.SoftClassifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrainersV2 {
    private static final Map<Class<? extends TrainerV2>, Class<? extends DecisionAnswererV2>> map = new HashMap<>();
    private static final Map<TrainerV2, String> dynamicMap = new HashMap<>();

    private static final List<TrainerV2> trainers;

    static {
        map.put(AnotherMoveTrainer.class, AnotherMoveAnswerer.class);
        map.put(MulliganTrainer.class, MulliganAnswerer.class);
        map.put(StartingFellowshipTrainer.class, StartingFellowshipAnswerer.class);
        map.put(PlaySiteTrainer.class, PlaySiteAnswerer.class);
        map.put(AttachItemTrainer.class, AttachItemAnswerer.class);
        map.put(FpArcherySelfWoundTrainer.class, FpArcherySelfWoundAnswerer.class);
        map.put(FpThreatSelfWoundTrainer.class, FpThreatSelfWoundAnswerer.class);
        map.put(ReconcileDiscardDownTrainer.class, ReconcileDiscardDownAnswerer.class);
        map.put(ReconcileDiscardOneTrainer.class, ReconcileDiscardOneAnswerer.class);
        map.put(SanctuaryHealTrainer.class, SanctuaryHealAnswerer.class);
        map.put(ShadowArcherySelfWoundTrainer.class, ShadowArcherySelfWoundAnswerer.class);
        map.put(SkirmishOrderTrainer.class, SkirmishOrderAnswerer.class);

        trainers = new ArrayList<>();
        for (Class<? extends TrainerV2> trainerClass : map.keySet()) {
            try {
                trainers.add(trainerClass.getDeclaredConstructor().newInstance());
            } catch (Exception e) {
                throw new RuntimeException("Failed to instantiate trainer: " + trainerClass.getName(), e);
            }
        }
    }

    private TrainersV2() {

    }

    public static void add(TrainerV2 trainer, DecisionAnswererV2 answerer) {
        trainers.add(trainer);
        dynamicMap.put(trainer, answerer.getName());
    }

    public static List<TrainerV2> getAllV2Trainers() {
        return new UnmodifiableList<>(trainers);
    }

    public static void trainModels(ModelRegistryV2 modelRegistry) {
        for (Map.Entry<Class<? extends TrainerV2>, Class<? extends DecisionAnswererV2>> trainerAnswererEntry : map.entrySet()) {
            try {
                TrainerV2 trainer = trainerAnswererEntry.getKey().getDeclaredConstructor().newInstance();
                List<SavedVector> vectors = SavedVectorPersistence.load(trainer);
                if (!vectors.isEmpty()) {
                    try {
                        SoftClassifier<double[]> model = trainer.train(vectors);
                        modelRegistry.registerModel(trainerAnswererEntry.getValue(), model);
                    } catch (IllegalArgumentException e) {
                        // If only not enough data for model, let it be
                        if (!e.getMessage().toLowerCase().contains("Only one class".toLowerCase())) {
                            throw e;
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to make model for trainer: " + trainerAnswererEntry.getKey().getSimpleName(), e);
            }
        }
        for (Map.Entry<TrainerV2, String> entry : dynamicMap.entrySet()) {
            try {
                TrainerV2 trainer = entry.getKey();
                List<SavedVector> vectors = SavedVectorPersistence.load(trainer);
                if (!vectors.isEmpty()) {
                    try {
                        SoftClassifier<double[]> model = trainer.train(vectors);
                        modelRegistry.registerModel(entry.getValue(), model);
                    } catch (IllegalArgumentException e) {
                        // If only not enough data for model, let it be
                        if (!e.getMessage().toLowerCase().contains("Only one class".toLowerCase())) {
                            throw e;
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to make model for trainer: " + entry.getKey().getName(), e);
            }

        }
    }
}
