package com.gempukku.lotro.bots.forge.plan.action;

import com.gempukku.lotro.logic.decisions.AwaitingDecision;

public class OptionalTriggerDenyAction implements ActionToTake {
    public OptionalTriggerDenyAction() {

    }

    @Override
    public String toString() {
        return "Action: Deny optional triggers";
    }

    @Override
    public int carryOut(AwaitingDecision awaitingDecision) {
        return -1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o != null && getClass() == o.getClass();
    }

    @Override
    public int hashCode() {
        return OptionalTriggerDenyAction.class.hashCode();
    }
}
