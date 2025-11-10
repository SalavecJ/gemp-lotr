package com.gempukku.lotro.bots.forge.cards.abstractcard;

import com.gempukku.lotro.bots.forge.cards.ability2.EventAbility;
import com.gempukku.lotro.bots.forge.cards.ability2.condition.Condition;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.bots.forge.plan.PlannedBoardState;

public abstract class BotEventCard extends BotCard {
    public BotEventCard(PhysicalCard self) {
        super(self);
    }

    @Override
    public boolean canBePlayedNoMatterThePhase(PlannedBoardState plannedBoardState) {
        return (getCondition() == null || getCondition().isOk(this.getSelf().getOwner(), plannedBoardState))
                && getEventAbility().canPayCost(this.getSelf().getOwner(), plannedBoardState);
    }

    public boolean isResponseEvent() {
        return self.getBlueprint().hasTimeword(Timeword.RESPONSE);
    }

    /**
     * Hook for subclasses to implement card-specific rules for current board state
     */
    public Condition getCondition() {
        return null;
    }

    @Override
    public abstract EventAbility getEventAbility();
}
