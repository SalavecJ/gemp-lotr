package com.gempukku.lotro.bots.forge.plan.action2;

import com.gempukku.lotro.bots.forge.cards.abstractcard.BotCard;

public class AcceptTriggerAction2 extends ChooseCardAction2 {

    public AcceptTriggerAction2(String decisionText, BotCard card, String actionId) {
        super(decisionText, card, actionId);
    }

    @Override
    public String toString() {
        return "Action: Accept trigger of " + getPhysicalCard().getBlueprint().getFullName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AcceptTriggerAction2 that = (AcceptTriggerAction2) o;
        return getPhysicalCard().getCardId() == that.getPhysicalCard().getCardId();
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(getPhysicalCard().getCardId());
    }
}
