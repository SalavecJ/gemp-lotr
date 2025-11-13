package com.gempukku.lotro.bots.forge.plan.action;

import com.gempukku.lotro.bots.forge.cards.abstractcard.BotCard;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

public class OptionalTriggerDenyAction implements ActionToTake {
    private final BotCard toDeny;

    public OptionalTriggerDenyAction(BotCard toDeny) {
        this.toDeny = toDeny;
    }

    public BotCard getSource() {
        return toDeny;
    }

    @Override
    public String toString() {
        return "Action: Deny trigger of " + toDeny.getSelf().getBlueprint().getFullName();
    }

    @Override
    public int carryOut(AwaitingDecision awaitingDecision) {
        return -1;
    }
}
