package com.gempukku.lotro.bots.forge.plan.action;

import com.gempukku.lotro.bots.forge.cards.abstractcards.BotCard;

public class DiscardToReconcileAction extends  ChooseCardAction {


    public DiscardToReconcileAction(String decisionText, BotCard card) {
        super(decisionText, card, String.valueOf(card.getPhysicalCard().getCardId()));
    }

    @Override
    public String toString() {
        return "Action: Choose " + getCard().getFullName() + " to discard to reconcile";
    }
}
