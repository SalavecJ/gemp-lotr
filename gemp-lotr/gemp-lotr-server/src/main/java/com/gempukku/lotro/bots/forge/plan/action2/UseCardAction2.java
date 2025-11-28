package com.gempukku.lotro.bots.forge.plan.action2;

import com.gempukku.lotro.bots.forge.cards.abstractcard.BotCard;

public class UseCardAction2 extends ChooseCardAction2 {

    public UseCardAction2(String decisionText, BotCard toUse, String actionId) {
        super(decisionText, toUse, actionId);
    }

    @Override
    public String toString() {
        return "Action: Use " + getPhysicalCard().getBlueprint().getFullName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UseCardAction2 that = (UseCardAction2) o;
        return getPhysicalCard().getCardId() == that.getPhysicalCard().getCardId();
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(getPhysicalCard().getCardId());
    }
}
