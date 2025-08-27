package com.gempukku.lotro.bots.forge.plan.action;

import com.gempukku.lotro.logic.decisions.AwaitingDecision;

public interface ActionToTake {
    int carryOut(AwaitingDecision awaitingDecision);
}
