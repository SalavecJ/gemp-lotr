package com.gempukku.lotro.bots.v2.decision.option;

public class MulliganOption extends Option {
    private final boolean mulligan;

    public MulliganOption(boolean mulligan, int index) {
        super((mulligan ? "Mulligan" : "Keep the cards"), String.valueOf(index));
        this.mulligan = mulligan;
    }

    public boolean mulligan() {
        return mulligan;
    }
}
