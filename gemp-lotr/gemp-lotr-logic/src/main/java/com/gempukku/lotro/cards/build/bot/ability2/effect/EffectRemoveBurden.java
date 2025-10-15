package com.gempukku.lotro.cards.build.bot.ability2.effect;

import com.gempukku.lotro.cards.build.bot.abstractcard.BotCard;
import com.gempukku.lotro.game.state.PlannedBoardState;

public class EffectRemoveBurden extends Effect{
    protected final int amount ;

    public EffectRemoveBurden(int amount) {
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public void resolve(BotCard source, PlannedBoardState plannedBoardState) {
        plannedBoardState.removeBurden(amount);
    }

    @Override
    public double getValueIfResolved(BotCard source, PlannedBoardState plannedBoardState) {
        int burdensPlaced = plannedBoardState.getBurdens();
        int toBeRemoved = Math.min(amount, burdensPlaced);
        double valueOfOneRemovedBurden = 0.9 + ((double) burdensPlaced / 10); // more burdens placed, better to remove
        double totalValue = toBeRemoved * valueOfOneRemovedBurden;
        // removing my burdens good, removing opponent's burdens bad
        return source.getSelf().getOwner().equals(plannedBoardState.getCurrentFpPlayer()) ? totalValue : -totalValue;
    }
}
