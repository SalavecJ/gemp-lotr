package com.gempukku.lotro.bots.v2.decision.option;

public class BurdenBidOption extends Option {
    private final int bid;

    public BurdenBidOption(int bid) {
        super("Bid " + bid + " burdens", String.valueOf(bid));
        this.bid = bid;
    }

    public int getBid() {
        return bid;
    }
}
