package com.gempukku.lotro.bots.forge.plan.action;

import com.gempukku.lotro.game.PhysicalCard;

public class OptionalTriggerAcceptAction extends ChooseCardAction {

    public OptionalTriggerAcceptAction(PhysicalCard toUse) {
        super(toUse);
    }

    @Override
    protected String actionPrefix() {
        return "Optional trigger";
    }

    @Override
    public String toString() {
        return "Action: Accept trigger of " + getCard().getBlueprint().getFullName();
    }
}
