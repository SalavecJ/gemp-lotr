package com.gempukku.lotro.cards.build.bot.abstractcard;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.PlannedBoardState;

public abstract class BotOneRingCard extends BotCard {
    public BotOneRingCard(PhysicalCard self) {
        super(self);
    }

    @Override
    public boolean canBePlayed(PlannedBoardState plannedBoardState) {
        throw new IllegalStateException("The One Ring card cannot be played.");
    }
}
