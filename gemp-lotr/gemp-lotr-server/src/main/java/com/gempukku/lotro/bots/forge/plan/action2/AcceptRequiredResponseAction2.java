package com.gempukku.lotro.bots.forge.plan.action2;

public class AcceptRequiredResponseAction2 extends ActionToTake2 {
    private final int actionId;
    private final String actionText;

    public AcceptRequiredResponseAction2(String decisionText, int actionId, String actionText) {
        super(decisionText);
        this.actionId = actionId;
        this.actionText = actionText;
    }

    @Override
    public String carryOut() {
        return String.valueOf(actionId);
    }

    @Override
    public String toString() {
        return "Action: " + actionText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o != null && getClass() == o.getClass();
    }

    @Override
    public int hashCode() {
        return AcceptRequiredResponseAction2.class.hashCode();
    }
}
