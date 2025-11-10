package com.gempukku.lotro.bots.forge.plan.action;

import com.gempukku.lotro.bots.forge.cards.abstractcard.BotCard;

public class DiscardCompanionToHealAction extends ChooseCardAction {

    public DiscardCompanionToHealAction(BotCard toDiscard) {
        super(toDiscard);
    }

    @Override
    protected String actionPrefix() {
        return "Heal";
    }

    @Override
    public String toString() {
        return "Action: Discard " + getPhysicalCard().getBlueprint().getFullName() + " from hand to heal companion in play";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiscardCompanionToHealAction that = (DiscardCompanionToHealAction) o;
        return getPhysicalCard().getCardId() == that.getPhysicalCard().getCardId();
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(getPhysicalCard().getCardId());
    }
}
