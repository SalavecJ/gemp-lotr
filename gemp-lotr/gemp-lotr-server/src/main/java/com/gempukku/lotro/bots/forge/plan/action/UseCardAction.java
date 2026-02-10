package com.gempukku.lotro.bots.forge.plan.action;

import com.gempukku.lotro.bots.forge.cards.abstractcards.BotCard;

public class UseCardAction extends ChooseCardAction {

    public UseCardAction(String decisionText, BotCard toUse, String actionId) {
        super(decisionText, toUse, actionId);
    }

    @Override
    public String toString() {
        return "Action: Use " + getPhysicalCard().getBlueprint().getFullName();
    }
}
