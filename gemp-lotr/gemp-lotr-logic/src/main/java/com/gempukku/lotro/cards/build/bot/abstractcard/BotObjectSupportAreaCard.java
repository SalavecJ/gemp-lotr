package com.gempukku.lotro.cards.build.bot.abstractcard;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

public abstract class BotObjectSupportAreaCard extends BotObjectCard {
    public BotObjectSupportAreaCard(PhysicalCard self) {
        super(self);
    }

    @Override
    protected final boolean canBearThis(LotroGame game, PhysicalCard target) {
        return false;
    }

    @Override
    protected boolean playsToSupportArea() {
        return false;
    }
}
