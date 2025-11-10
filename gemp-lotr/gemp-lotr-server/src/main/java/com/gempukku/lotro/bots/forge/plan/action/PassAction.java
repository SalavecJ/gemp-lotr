package com.gempukku.lotro.bots.forge.plan.action;

import com.gempukku.lotro.logic.decisions.AwaitingDecision;

public class PassAction implements ActionToTake {
    @Override
    public int carryOut(AwaitingDecision awaitingDecision) {
        return -1;
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
        return PassAction.class.hashCode();
    }
}
