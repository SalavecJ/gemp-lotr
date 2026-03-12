package com.gempukku.lotro.bots.forge.plan.action;

import com.gempukku.lotro.bots.forge.cards.abstractcards.BotCard;

public class ChooseSkirmishAction extends ChooseCardAction {

    public ChooseSkirmishAction(String decisionText, BotCard card) {
        super(decisionText, card, String.valueOf(card.getPhysicalCard().getCardId()));
    }

    @Override
    public String toString() {
        return "Action: Choose " + getCard().getFullName() + " as next skirmish to resolve";
    }
}
