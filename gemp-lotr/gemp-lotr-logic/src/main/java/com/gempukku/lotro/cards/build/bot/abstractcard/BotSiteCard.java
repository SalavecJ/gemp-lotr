package com.gempukku.lotro.cards.build.bot.abstractcard;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

public abstract class BotSiteCard extends BotCard {
    public BotSiteCard(PhysicalCard self) {
        super(self);
    }

    @Override
    public boolean canBePlayed(LotroGame game) {
        throw new IllegalStateException("The bot should not ask if site card can be played.");
    }

    @Override
    public boolean canEverBePlayed(LotroGame game) {
        throw new IllegalStateException("The bot should not ask if site card can be played.");
    }
}
