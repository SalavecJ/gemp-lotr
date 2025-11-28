package com.gempukku.lotro.bots.forge.plan.action2;

public class PassAction2 extends ActionToTake2 {
    public PassAction2(String decisionText) {
        super(decisionText);
    }

    @Override
    public String carryOut() {
        return "";
    }

    @Override
    public String toString() {
        return "Action: Pass";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o != null && getClass() == o.getClass();
    }

    @Override
    public int hashCode() {
        return PassAction2.class.hashCode();
    }
}
