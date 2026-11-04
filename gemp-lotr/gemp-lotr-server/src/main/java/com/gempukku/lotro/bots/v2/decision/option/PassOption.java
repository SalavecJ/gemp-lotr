package com.gempukku.lotro.bots.v2.decision.option;

public class PassOption extends Option {
    public PassOption() {
        super("Pass", "");
    }

    @Override
    public int hashCode() {
        return PassOption.class.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && getClass() == obj.getClass();
    }
}
