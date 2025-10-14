package com.gempukku.lotro.bots.forge.plan.action;

import com.gempukku.lotro.game.PhysicalCard;

public class UseCardWithTargetAction extends ChooseCardAction {
    private final PhysicalCard target;

    public UseCardWithTargetAction(PhysicalCard toUse, PhysicalCard target) {
        super(toUse);
        this.target = target;
    }

    public PhysicalCard getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return "Action: Use " + getCard().getBlueprint().getFullName() + " on " + target.getBlueprint().getFullName();
    }
}
