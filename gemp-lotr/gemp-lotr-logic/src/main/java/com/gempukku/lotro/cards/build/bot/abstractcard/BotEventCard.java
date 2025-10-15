package com.gempukku.lotro.cards.build.bot.abstractcard;

import com.gempukku.lotro.cards.build.bot.ability2.EventAbility;
import com.gempukku.lotro.cards.build.bot.ability2.condition.Condition;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.PlannedBoardState;

public abstract class BotEventCard extends BotCard {
    public BotEventCard(PhysicalCard self) {
        super(self);
    }

    @Override
    public boolean canBePlayed(PlannedBoardState plannedBoardState) {
        return (getCondition() == null || getCondition().isOk(this, plannedBoardState))
                && getEventAbility().canPayCost(this, plannedBoardState)
                && isPlayableInPhase(plannedBoardState.getCurrentPhase());
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
