package com.gempukku.lotro.bots.forge.plan.action;

public class DenyTriggerAction extends ActionToTake {

    public DenyTriggerAction(String decisionText) {
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
}
