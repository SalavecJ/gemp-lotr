package com.gempukku.lotro.bots.forge.plan.action;

import com.gempukku.lotro.bots.forge.cards.abstractcard.BotCard;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

public class ChooseTargetAction implements ActionToTake {
    private final BotCard target;

    public ChooseTargetAction(BotCard target) {
        this.target = target;
    }

    public BotCard getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return "Action: Choose as a target - " + target.getSelf().getBlueprint().getFullName();
    }

    @Override
    public int carryOut(AwaitingDecision awaitingDecision) {
        return target.getSelf().getCardId();
    }
}
