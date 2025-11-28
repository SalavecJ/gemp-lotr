package com.gempukku.lotro.bots.forge.cards.abstractcard;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

public abstract class BotObjectSupportAreaCard extends BotObjectCard {
    public BotObjectSupportAreaCard(PhysicalCard self) {
        super(self);
    }

    @Override
    protected final boolean canBearThis(DefaultLotroGame game, PhysicalCard target) {
        return false;
    }

    @Override
    protected boolean playsToSupportArea() {
        return true;
    }
}
