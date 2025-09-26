package com.gempukku.lotro.cards.build.bot.abstractcard;

import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.state.PlannedBoardState;

public abstract class BotEventCard extends BotCard {
    public BotEventCard(PhysicalCard self) {
        super(self);
    }

    @Override
    public boolean canBePlayed(PlannedBoardState plannedBoardState) {
        return otherRequirementsNowOk(plannedBoardState);
    }

    @Override
    public boolean canEverBePlayed() {
        return otherRequirementsEverOk(self.getGame());
    }

    public boolean isResponseEvent() {
        return self.getBlueprint().hasTimeword(Timeword.RESPONSE);
    }

    /**
     * Hook for subclasses to implement card-specific rules for current board state
     */
    protected boolean otherRequirementsNowOk(PlannedBoardState plannedBoardState) {
        return true;
    }

    /**
     * Hook for subclasses to implement card-specific rules for potential play
     */
    protected boolean otherRequirementsEverOk(LotroGame game) {
        return true;
    }
}
