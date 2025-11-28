package com.gempukku.lotro.bots.forge.cards.abstractcard;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

public abstract class BotCompanionCard extends BotCharacterCard {
    public BotCompanionCard(PhysicalCard self) {
        super(self);
    }

    @Override
    public boolean canBePlayedNoMatterThePhase(DefaultLotroGame game) {
        return super.canBePlayedNoMatterThePhase(game) && ruleOfNineOk(game);
    }

    private boolean ruleOfNineOk(DefaultLotroGame game) {
        return game.getGameState().ruleOfNineRemainder(self.getOwner()) > 0;
    }
}
