package com.gempukku.lotro.bots.forge.plan.action;

public abstract class ActionToTake {
    private final String decisionText;

    public ActionToTake(String decisionText) {
        this.decisionText = decisionText;
    }

    public String getDecisionText() {
        return decisionText;
    }

    public abstract String carryOut();

    public abstract String toString();
}
