package com.gempukku.lotro.bots.forge.plan.action2;

public abstract class ActionToTake2 {
    private final String decisionText;

    public ActionToTake2(String decisionText) {
        this.decisionText = decisionText;
    }

    public String getDecisionText() {
        return decisionText;
    }

    public abstract String carryOut();
}
