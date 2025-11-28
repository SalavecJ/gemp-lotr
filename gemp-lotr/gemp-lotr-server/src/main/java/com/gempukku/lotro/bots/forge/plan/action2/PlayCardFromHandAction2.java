package com.gempukku.lotro.bots.forge.plan.action2;

import com.gempukku.lotro.bots.forge.cards.abstractcard.BotCard;

public class PlayCardFromHandAction2 extends ChooseCardAction2 {

    public PlayCardFromHandAction2(String decisionText, BotCard toPlay, String actionId) {
        super(decisionText, toPlay, actionId);
    }

    @Override
    public String toString() {
        return "Action: Play " + getPhysicalCard().getBlueprint().getFullName() + " from hand";
    }

    public boolean playsTheSameCard(PlayCardFromHandAction2 action) {
        return this.getPhysicalCard().getBlueprintId().equals(action.getPhysicalCard().getBlueprintId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayCardFromHandAction2 that = (PlayCardFromHandAction2) o;
        return getPhysicalCard().getCardId() == that.getPhysicalCard().getCardId();
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(getPhysicalCard().getCardId());
    }
}
