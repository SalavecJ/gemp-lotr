package com.gempukku.lotro.cards.build.bot.abstractcard;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.PlannedBoardState;

public abstract class BotSiteCard extends BotCard {
    public BotSiteCard(PhysicalCard self) {
        super(self);
    }

    @Override
    public boolean canBePlayedNoMatterThePhase(PlannedBoardState plannedBoardState) {
        throw new IllegalStateException("The bot should not ask if site card can be played.");
    }
}
