package com.gempukku.lotro.bots.v2.decision.option;

public class StartingPositionOption extends Option {
    private final int startingPosition;

    public StartingPositionOption(String option, int index) {
        super("Choose starting position: " + option, String.valueOf(index));
        if (option.contains("first")) {
            startingPosition = 1;
        } else if (option.contains("second")) {
            startingPosition = 2;
        } else {
            throw new IllegalArgumentException("Unknown starting position: " + option);
        }
    }

    public int getStartingPosition() {
        return startingPosition;
    }
}
