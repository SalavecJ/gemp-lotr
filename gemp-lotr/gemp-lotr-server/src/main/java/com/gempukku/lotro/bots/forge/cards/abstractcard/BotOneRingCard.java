package com.gempukku.lotro.bots.forge.cards.abstractcard;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

public abstract class BotOneRingCard extends BotCard {
    public BotOneRingCard(PhysicalCard self) {
        super(self);
    }

    @Override
    public boolean canBePlayedNoMatterThePhase(DefaultLotroGame game) {
        throw new IllegalStateException("The One Ring card cannot be played.");
    }
}
