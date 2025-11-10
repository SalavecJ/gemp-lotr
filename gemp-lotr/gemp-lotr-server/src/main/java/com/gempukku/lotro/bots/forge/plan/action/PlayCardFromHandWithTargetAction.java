package com.gempukku.lotro.bots.forge.plan.action;

import com.gempukku.lotro.bots.forge.cards.abstractcard.BotCard;
import com.gempukku.lotro.game.PhysicalCard;

public class PlayCardFromHandWithTargetAction extends PlayCardFromHandAction {
    private final PhysicalCard target;

    public PlayCardFromHandWithTargetAction(BotCard toPlay, PhysicalCard target) {
        super(toPlay);
        this.target = target;
    }

    public PhysicalCard getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return "Action: Play " + getPhysicalCard().getBlueprint().getFullName() + " from hand on " + target.getBlueprint().getFullName();
    }
}
