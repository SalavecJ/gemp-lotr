package com.gempukku.lotro.draft3.timer;

import java.util.HashMap;
import java.util.Map;

public class DraftTimerFast implements DraftTimer{
    private Map<Integer, Integer> durations = new HashMap<>();

    public DraftTimerFast() {
        // MtG timer from Tournament Guides until 15 cards, then extended to 20
        durations.put(1, 5);
        durations.put(2, 5);
        durations.put(3, 5);
        durations.put(4, 5);
        durations.put(5, 10);
        durations.put(6, 10);
        durations.put(7, 15);
        durations.put(8, 20);
        durations.put(9, 20);
        durations.put(10, 25);
        durations.put(11, 25);
        durations.put(12, 30);
        durations.put(13, 35);
        durations.put(14, 40);
        durations.put(15, 40);
        durations.put(16, 45);
        durations.put(17, 45);
        durations.put(18, 50);
        durations.put(19, 50);
        durations.put(20, 55);
    }

    @Override
    public int getSecondsAllotted(int cardsInPack) {
        // Unknown duration
        if (!durations.containsKey(cardsInPack)) {
            return -1;
        }

        // Look to map for info
        return durations.get(cardsInPack);
    }
}
