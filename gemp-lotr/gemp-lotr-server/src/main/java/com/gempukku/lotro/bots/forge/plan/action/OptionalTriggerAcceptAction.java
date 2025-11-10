package com.gempukku.lotro.bots.forge.plan.action;

import com.gempukku.lotro.bots.forge.cards.abstractcard.BotCard;

public class OptionalTriggerAcceptAction extends ChooseCardAction {

    public OptionalTriggerAcceptAction(BotCard toUse) {
        super(toUse);
    }

    @Override
    protected String actionPrefix() {
        return "Optional trigger";
    }

    @Override
    public String toString() {
        return "Action: Accept trigger of " + getPhysicalCard().getBlueprint().getFullName();
    }
}
