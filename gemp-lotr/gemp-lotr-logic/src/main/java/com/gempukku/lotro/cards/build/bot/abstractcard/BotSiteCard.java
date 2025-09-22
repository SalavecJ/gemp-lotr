package com.gempukku.lotro.cards.build.bot.abstractcard;

import com.gempukku.lotro.game.PhysicalCard;

public abstract class BotSiteCard extends BotCard {
    public BotSiteCard(PhysicalCard self) {
        super(self);
    }

    @Override
    public boolean canBePlayed() {
        throw new IllegalStateException("The bot should not ask if site card can be played.");
    }

    @Override
    public boolean canEverBePlayed() {
        throw new IllegalStateException("The bot should not ask if site card can be played.");
    }
}
