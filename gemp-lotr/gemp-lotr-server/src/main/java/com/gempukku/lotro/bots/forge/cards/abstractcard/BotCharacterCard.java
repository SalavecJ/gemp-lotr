package com.gempukku.lotro.bots.forge.cards.abstractcard;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

public abstract class BotCharacterCard extends BotCard {
    public BotCharacterCard(PhysicalCard self) {
        super(self);
    }

    @Override
    public boolean canBePlayedNoMatterThePhase(DefaultLotroGame game) {
        return uniqueRequirementOk(game);
    }
}
