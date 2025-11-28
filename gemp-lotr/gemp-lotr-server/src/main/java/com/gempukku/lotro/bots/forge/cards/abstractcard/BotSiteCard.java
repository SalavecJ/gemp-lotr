package com.gempukku.lotro.bots.forge.cards.abstractcard;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

public abstract class BotSiteCard extends BotCard {
    public BotSiteCard(PhysicalCard self) {
        super(self);
    }

    @Override
    public boolean canBePlayedNoMatterThePhase(DefaultLotroGame game) {
        throw new IllegalStateException("The bot should not ask if site card can be played.");
    }
}
