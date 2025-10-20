package com.gempukku.lotro.cards.build.bot.abstractcard;

import com.gempukku.lotro.cards.build.bot.ability2.condition.Condition;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.PlannedBoardState;

public abstract class BotCharacterCard extends BotCard {
    public BotCharacterCard(PhysicalCard self) {
        super(self);
    }

    @Override
    public boolean canBePlayedNoMatterThePhase(PlannedBoardState plannedBoardState) {
        if (!uniqueRequirementOk(plannedBoardState)) return false;
        return (getCondition() == null || getCondition().isOk(this, plannedBoardState));
    }

    /**
     * Hook for subclasses to implement card-specific rules for current board state
     */
    public Condition getCondition() {
        return null;
    }
}
