package com.gempukku.lotro.cards.build.bot.abstractcard;

import com.gempukku.lotro.game.PhysicalCard;

public abstract class BotOneRingCard extends BotCard {
    public BotOneRingCard(PhysicalCard self) {
        super(self);
    }

    @Override
    public boolean canBePlayed() {
        throw new IllegalStateException("The One Ring card cannot be played.");
    }

    @Override
    public boolean canEverBePlayed() {
        throw new IllegalStateException("The One Ring card cannot be played.");
    }
}
