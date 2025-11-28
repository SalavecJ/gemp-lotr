package com.gempukku.lotro.bots.forge.plan.action2;

public class DenyTriggerAction2 extends ActionToTake2 {
    public DenyTriggerAction2(String decisionText) {
        super(decisionText);
    }

    @Override
    public String carryOut() {
        return "";
    }

    @Override
    public String toString() {
        return "Action: Deny optional triggers";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o != null && getClass() == o.getClass();
    }

    @Override
    public int hashCode() {
        return DenyTriggerAction2.class.hashCode();
    }
}
