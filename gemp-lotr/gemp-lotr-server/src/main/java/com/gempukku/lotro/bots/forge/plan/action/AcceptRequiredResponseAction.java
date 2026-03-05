package com.gempukku.lotro.bots.forge.plan.action;

import com.gempukku.lotro.bots.forge.cards.abstractcards.BotCard;

public class AcceptRequiredResponseAction extends ChooseCardAction {
    public AcceptRequiredResponseAction(String decisionText, BotCard card, String actionId) {
        super(decisionText, card, actionId);
    }

    @Override
    public String toString() {
        return "Action: Accept required response of " + getPhysicalCard().getBlueprint().getFullName();
    }
}
