package com.gempukku.lotro.cards.build.bot.abstractcard;

import com.gempukku.lotro.cards.build.bot.ability2.condition.Condition;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.PlannedBoardState;

public abstract class BotCharacterCard extends BotCard {
    public BotCharacterCard(PhysicalCard self) {
        super(self);
    }

    @Override
    public boolean canBePlayed(PlannedBoardState plannedBoardState) {
        if (!uniqueRequirementOk(plannedBoardState)) return false;
        if (!isPlayableInPhase(plannedBoardState.getCurrentPhase())) return false;
        return (getCondition() == null || getCondition().isOk(this, plannedBoardState));
    }

    private boolean uniqueRequirementOk(PlannedBoardState plannedBoardState) {
        return !plannedBoardState.sameTitleInPlayOrInDeadPile(self.getBlueprint().getTitle(), self.getOwner());
    }

    /**
     * Hook for subclasses to implement card-specific rules for current board state
     */
    public Condition getCondition() {
        return null;
    }
}
