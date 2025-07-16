package com.gempukku.lotro.bots.rl.v2.learning;

import java.util.ArrayList;
import java.util.List;

public class SavedVectorBuffer {
    private final List<SavedVector> buffer = new ArrayList<>();
    private final int capacity;

    public SavedVectorBuffer(int capacity) {
        this.capacity = capacity;
    }

    public void addEpisode(List<SavedVector> vectors) {
        if (vectors == null || vectors.isEmpty())
            return;

        buffer.addAll(vectors);

        if (buffer.size() > capacity) {
            SavedVectorPersistence.save(buffer);
            buffer.clear();
        }
    }

    public void save() {
        SavedVectorPersistence.save(buffer);
        buffer.clear();
    }
}
