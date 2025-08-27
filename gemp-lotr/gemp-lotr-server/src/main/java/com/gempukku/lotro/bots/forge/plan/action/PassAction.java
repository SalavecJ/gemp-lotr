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
}
