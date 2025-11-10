package com.gempukku.lotro.bots.forge.cards.abstractcard;

import com.gempukku.lotro.bots.forge.cards.ability2.condition.Condition;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.bots.forge.plan.PlannedBoardState;

public abstract class BotCharacterCard extends BotCard {
    public BotCharacterCard(PhysicalCard self) {
        super(self);
    }

    @Override
    public boolean canBePlayedNoMatterThePhase(PlannedBoardState plannedBoardState) {
        if (!uniqueRequirementOk(plannedBoardState)) return false;
        return (getCondition() == null || getCondition().isOk(this.getSelf().getOwner(), plannedBoardState));
    }

    /**
     * Hook for subclasses to implement card-specific rules for current board state
     */
    public Condition getCondition() {
        return null;
    }
}
