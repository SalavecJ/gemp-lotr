package com.gempukku.lotro.bots.forge.plan.action;

import com.gempukku.lotro.bots.forge.cards.abstractcard.BotCard;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

import java.util.List;

public abstract class ChooseTargetsAction implements ActionToTake {
    private final List<BotCard> targets;

    public ChooseTargetsAction(List<BotCard> targets) {
        this.targets = targets;
    }

    public List<BotCard> getTargets() {
        return targets;
    }

    @Override
    public int carryOut(AwaitingDecision awaitingDecision) {
        throw new IllegalStateException("Cannot carry out ChooseTargetAction directly");
    }
}
