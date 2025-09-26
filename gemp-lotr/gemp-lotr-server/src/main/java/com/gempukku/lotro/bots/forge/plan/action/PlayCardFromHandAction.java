package com.gempukku.lotro.bots.forge.plan.action;

import com.gempukku.lotro.game.PhysicalCard;

public class PlayCardFromHandAction extends ChooseCardAction {

    public PlayCardFromHandAction(PhysicalCard toPlay) {
        super(toPlay);
    }

    @Override
    public String toString() {
        return "Action: Play " + getCard().getBlueprint().getFullName() + " from hand";
    }
}
