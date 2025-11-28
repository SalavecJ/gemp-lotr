package com.gempukku.lotro.bots.forge.plan.action2;

import com.gempukku.lotro.bots.forge.cards.abstractcard.BotCard;

public class DiscardCompanionToHealAction2 extends ChooseCardAction2 {

    public DiscardCompanionToHealAction2(String decisionText, BotCard toDiscard, String actionId) {
        super(decisionText, toDiscard, actionId);
    }

    @Override
    public String toString() {
        return "Action: Discard " + getPhysicalCard().getBlueprint().getFullName() + " from hand to heal companion in play";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiscardCompanionToHealAction2 that = (DiscardCompanionToHealAction2) o;
        return getPhysicalCard().getCardId() == that.getPhysicalCard().getCardId();
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(getPhysicalCard().getCardId());
    }
}
