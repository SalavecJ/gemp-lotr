package com.gempukku.lotro.bots.forge.plan.action;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

public class OptionalTriggerDenyAction implements ActionToTake {
    private final PhysicalCard toDeny;

    public OptionalTriggerDenyAction(PhysicalCard toDeny) {
        this.toDeny = toDeny;
    }

    @Override
    public String toString() {
        return "Action: Deny trigger of " + toDeny.getBlueprint().getFullName();
    }

    @Override
    public int carryOut(AwaitingDecision awaitingDecision) {
        return -1;
    }
}
