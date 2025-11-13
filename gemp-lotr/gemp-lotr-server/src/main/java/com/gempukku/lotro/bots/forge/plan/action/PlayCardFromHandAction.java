package com.gempukku.lotro.bots.forge.plan.action;

import com.gempukku.lotro.bots.forge.cards.abstractcard.BotCard;

public class PlayCardFromHandAction extends ChooseCardAction {

    public PlayCardFromHandAction(BotCard toPlay) {
        super(toPlay);
    }

    @Override
    protected String actionPrefix() {
        return "Play";
    }

    @Override
    public String toString() {
        return "Action: Play " + getPhysicalCard().getBlueprint().getFullName() + " from hand";
    }

    public boolean playsTheSameCard(PlayCardFromHandAction action) {
        return this.getPhysicalCard().getBlueprintId().equals(action.getPhysicalCard().getBlueprintId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayCardFromHandAction that = (PlayCardFromHandAction) o;
        return getPhysicalCard().getCardId() == that.getPhysicalCard().getCardId();
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(getPhysicalCard().getCardId());
    }
}
