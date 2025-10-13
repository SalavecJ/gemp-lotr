package com.gempukku.lotro.bots.forge.plan.action;

import com.gempukku.lotro.game.PhysicalCard;

public class UseCardAction extends ChooseCardAction {

    public UseCardAction(PhysicalCard toUse) {
        super(toUse);
    }

    @Override
    public String toString() {
        return "Action: Use " + getCard().getBlueprint().getFullName();
    }
}
