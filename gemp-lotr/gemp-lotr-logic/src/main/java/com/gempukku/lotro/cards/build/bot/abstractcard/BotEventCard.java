package com.gempukku.lotro.cards.build.bot.abstractcard;

import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

public abstract class BotEventCard extends BotCard {
    public BotEventCard(PhysicalCard self) {
        super(self);
    }

    @Override
    public boolean canBePlayed() {
        if (!phaseOk(self.getGame())) return false;
        return otherRequirementsNowOk(self.getGame());
    }

    @Override
    public boolean canEverBePlayed() {
        return otherRequirementsEverOk(self.getGame());
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
