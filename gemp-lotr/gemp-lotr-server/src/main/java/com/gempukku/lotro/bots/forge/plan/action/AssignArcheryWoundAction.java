package com.gempukku.lotro.bots.forge.plan.action;

import com.gempukku.lotro.bots.forge.cards.abstractcards.BotCard;

public class AssignArcheryWoundAction extends  ChooseCardAction {


    public AssignArcheryWoundAction(String decisionText, BotCard card) {
        super(decisionText, card, String.valueOf(card.getPhysicalCard().getCardId()));
    }

    @Override
    public String toString() {
        return "Action: Choose " + getCard().getFullName() + " to be wounded by archery fire";
    }
}
