package com.gempukku.lotro.bots.rl.v2.learning;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SavedVectorPersistence {
    private SavedVectorPersistence() {
    }

    private static final Map<String, String> trainerFileMap =
            TrainersV2.getAllV2Trainers().stream().collect(Collectors.toMap(
                    TrainerV2::getName,
                    SavedVectorPersistence::generateFileName
            ));

    private static String generateFileName(TrainerV2 trainer) {
        String base = trainer.getName()
                .replaceAll("Trainer$", "") // Remove "Trainer" suffix
                .replaceAll("([a-z])([A-Z])", "$1-$2") // CamelCase to kebab-case
                .toLowerCase();
        return "trainer-v2-" + base + ".jsonl";
    }

    public static void save(List<SavedVector> vectors) {
        for (SavedVector vector : vectors) {
            for (Map.Entry<String, String> entry : trainerFileMap.entrySet()) {
                if (entry.getKey().equals(vector.className)) {
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
        String fileName = trainerFileMap.get(trainer.getName());
        if (fileName == null)
            return List.of();

        List<SavedVector> vectors = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                vectors.add(SavedVector.fromJson(line));
            }
        } catch (FileNotFoundException e) {
            // No decisions recorded by this trainer
            return List.of();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return vectors;
    }
}
