package com.gempukku.lotro.bots.forge.plan.action;

public class PassAction extends ActionToTake {

    public PassAction(String decisionText) {
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
}
