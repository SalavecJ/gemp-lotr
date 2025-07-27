package com.gempukku.lotro.bots.rl.v2.learning;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SavedVectorPersistence {
    private static final String VECTOR_DIR = "saved-vectors";

    static {
        try {
            Files.createDirectories(Paths.get(VECTOR_DIR));
        } catch (IOException e) {
            throw new RuntimeException("Failed to create model directory", e);
        }
    }

    private SavedVectorPersistence() {
    }

    private static Map<String, String> getTrainerFileMap() {
        Map<String, String> tbr = new HashMap<>();
        tbr.putAll(TrainersV2.getAllV2Trainers().stream().collect(Collectors.toMap(
                TrainerV2::getName,
                SavedVectorPersistence::generateFileName
        )));
        tbr.putAll(TrainersV2.getAllV2GeneralTrainers().stream().collect(Collectors.toMap(
                TrainerV2::getName,
                SavedVectorPersistence::generateFileName
        )));
        tbr.putAll(TrainersV2.getAllV2SubTrainers().stream().collect(Collectors.toMap(
                TrainerV2::getName,
                SavedVectorPersistence::generateFileName
        )));
        return tbr;
    }

    private static String generateFileName(TrainerV2 trainer) {
        String base = trainer.getName()
                .replaceAll("Trainer$", "") // Remove "Trainer" suffix
                .replaceAll("([a-z])([A-Z])", "$1-$2") // CamelCase to kebab-case
                .toLowerCase();
        return "trainer-v2-" + base + ".jsonl";
    }

    public static void save(List<SavedVector> vectors) {
        for (SavedVector vector : vectors) {
            for (Map.Entry<String, String> entry : getTrainerFileMap().entrySet()) {
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
        try (FileWriter fw = new FileWriter(new File(VECTOR_DIR, filename), true)) {
            fw.write(vector.toJson() + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<SavedVector> load(TrainerV2 trainer) {
        String fileName = getTrainerFileMap().get(trainer.getName());
        if (fileName == null)
            return List.of();

        List<SavedVector> vectors = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(new File(VECTOR_DIR, fileName)))) {
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
