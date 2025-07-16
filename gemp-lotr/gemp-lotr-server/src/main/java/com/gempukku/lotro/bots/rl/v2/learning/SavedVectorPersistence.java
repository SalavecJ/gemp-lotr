package com.gempukku.lotro.bots.rl.v2.learning;

import com.gempukku.lotro.bots.rl.learning.Trainer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SavedVectorPersistence {
    private SavedVectorPersistence() {
    }

    private static final Map<Class<? extends TrainerV2>, String> trainerFileMap =
            TrainersV2.getAllV2TrainerClasses().stream().collect(Collectors.toMap(
                    Function.identity(),
                    SavedVectorPersistence::generateFileName
            ));

    private static String generateFileName(Class<? extends TrainerV2> cls) {
        String base = cls.getSimpleName()
                .replaceAll("Trainer$", "") // Remove "Trainer" suffix
                .replaceAll("([a-z])([A-Z])", "$1-$2") // CamelCase to kebab-case
                .toLowerCase();
        return "trainer-v2-" + base + ".jsonl";
    }

    public static void save(List<SavedVector> vectors) {
        for (SavedVector vector : vectors) {
            for (Map.Entry<Class<? extends TrainerV2>, String> entry : trainerFileMap.entrySet()) {
                if (entry.getKey().getSimpleName().equals(vector.className)) {
                    String filename = entry.getValue();
                    try {
                        saveVectorsToFile(filename, vector);
                    } catch (Exception e) {
                        e.printStackTrace(); // or log
                    }
                }
            }
        }
    }

    private static void saveVectorsToFile(String filename, SavedVector vector) {
        try (FileWriter fw = new FileWriter(filename, true)) {
            fw.write(vector.toJson() + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<SavedVector> load(TrainerV2 trainer) {
        String fileName = trainerFileMap.get(trainer.getClass());
        if (fileName == null)
            return List.of();

        List<SavedVector> vectors = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                vectors.add(SavedVector.fromJson(line));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return vectors;
    }
}
