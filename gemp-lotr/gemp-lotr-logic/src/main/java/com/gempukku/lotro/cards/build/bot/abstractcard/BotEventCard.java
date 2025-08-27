package com.gempukku.lotro.cards.build.bot.abstractcard;

import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

public abstract class BotEventCard extends BotCard {
    public BotEventCard(PhysicalCard self) {
        super(self);
    }

    @Override
    public boolean canBePlayed(LotroGame game) {
        if (!phaseOk(game)) return false;
        return otherRequirementsNowOk(game);
    }

    @Override
    public boolean canEverBePlayed(LotroGame game) {
        return otherRequirementsEverOk(game);
    }

    public boolean isResponseEvent() {
        return self.getBlueprint().hasTimeword(Timeword.RESPONSE);
    }

    private boolean phaseOk(LotroGame game) {
        return self.getBlueprint().hasTimeword(Timeword.findByPhase(game.getGameState().getCurrentPhase())) || isResponseEvent();
    }

    /**
     * Hook for subclasses to implement card-specific rules for current board state
     */
    protected boolean otherRequirementsNowOk(LotroGame game) {
        return true;
    }

    /**
     * Hook for subclasses to implement card-specific rules for potential play
     */
    protected boolean otherRequirementsEverOk(LotroGame game) {
        return true;
    }
}
