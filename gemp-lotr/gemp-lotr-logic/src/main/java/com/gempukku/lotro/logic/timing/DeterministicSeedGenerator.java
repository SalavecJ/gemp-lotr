package com.gempukku.lotro.logic.timing;

import java.util.Random;

public class DeterministicSeedGenerator {
    private final Random rng;
    private final long masterSeed;
    private int callCount;

    public DeterministicSeedGenerator(long masterSeed) {
        this.masterSeed = masterSeed;
        this.rng = new Random(masterSeed);
        this.callCount = 0;
    }

    public DeterministicSeedGenerator(DeterministicSeedGenerator other) {
        this.masterSeed = other.masterSeed;
        this.callCount = other.callCount;
        // Recreate RNG at same position
        this.rng = new Random(this.masterSeed);
        for (int i = 0; i < this.callCount; i++) {
            this.rng.nextLong(); // advance to same position
        }
    }

    public long nextSeed() {
        callCount++;
        return rng.nextLong();
    }
}
