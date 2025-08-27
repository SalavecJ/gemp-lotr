package com.gempukku.lotro.bots.forge.plan.action;

import com.gempukku.lotro.logic.decisions.AwaitingDecision;

public class ReplanAction implements ActionToTake {
    @Override
    public int carryOut(AwaitingDecision awaitingDecision) {
        throw new IllegalStateException("Replanning request");
    }

    @Override
    public String toString() {
        return "Action: Replanning";
    }
}
