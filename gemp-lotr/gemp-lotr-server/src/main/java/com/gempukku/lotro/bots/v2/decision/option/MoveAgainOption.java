package com.gempukku.lotro.bots.v2.decision.option;

public class MoveAgainOption extends Option {
    private final boolean moveAgain;

    public MoveAgainOption(boolean moveAgain, int index) {
        super((moveAgain ? "Make another move" : "Stay"), String.valueOf(index));
        this.moveAgain = moveAgain;
    }

    public boolean moveAgain() {
        return moveAgain;
    }
}
