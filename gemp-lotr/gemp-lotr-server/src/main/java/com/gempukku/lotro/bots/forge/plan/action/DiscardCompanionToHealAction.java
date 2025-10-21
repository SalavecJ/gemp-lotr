package com.gempukku.lotro.bots.forge.plan.action;

import com.gempukku.lotro.game.PhysicalCard;

public class DiscardCompanionToHealAction extends ChooseCardAction {

    public DiscardCompanionToHealAction(PhysicalCard toDiscard) {
        super(toDiscard);
    }

    @Override
    protected String actionPrefix() {
        return "Heal";
    }

    @Override
    public String toString() {
        return "Action: Discard " + getCard().getBlueprint().getFullName() + " from hand to heal companion in play";
    }
}
