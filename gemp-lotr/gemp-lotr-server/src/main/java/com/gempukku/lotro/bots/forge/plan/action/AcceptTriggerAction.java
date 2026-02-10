package com.gempukku.lotro.bots.forge.plan.action;

import com.gempukku.lotro.bots.forge.cards.abstractcards.BotCard;

public class AcceptTriggerAction extends ChooseCardAction {
    public AcceptTriggerAction(String decisionText, BotCard card, String actionId) {
        super(decisionText, card, actionId);
    }

    @Override
    public String toString() {
        return "Action: Accept trigger of " + getPhysicalCard().getBlueprint().getFullName();
    }
}
