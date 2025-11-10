package com.gempukku.lotro.bots.forge.cards.abstractcard;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.bots.forge.plan.PlannedBoardState;

public abstract class BotObjectSupportAreaCard extends BotObjectCard {
    public BotObjectSupportAreaCard(PhysicalCard self) {
        super(self);
    }

    @Override
    protected final boolean canBearThis(PlannedBoardState plannedBoardState, BotCard target) {
        return false;
    }

    @Override
    protected boolean playsToSupportArea() {
        return true;
    }
}
