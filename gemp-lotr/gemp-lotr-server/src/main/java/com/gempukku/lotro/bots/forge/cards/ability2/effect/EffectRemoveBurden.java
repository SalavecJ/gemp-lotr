package com.gempukku.lotro.bots.forge.cards.ability2.effect;

import com.gempukku.lotro.bots.forge.plan.PlannedBoardState;

public class EffectRemoveBurden extends Effect{
    protected final int amount ;

    public EffectRemoveBurden(int amount) {
        this.amount = amount;
    }

    @Override
    public void resolve(String player, PlannedBoardState plannedBoardState) {
        plannedBoardState.removeBurden(amount);
    }

    @Override
    public double getValueIfResolved(String player, PlannedBoardState plannedBoardState) {
        int burdensPlaced = plannedBoardState.getBurdens();
        int toBeRemoved = Math.min(amount, burdensPlaced);
        double valueOfOneRemovedBurden = 0.9 + ((double) burdensPlaced / 10); // more burdens placed, better to remove
        double totalValue = toBeRemoved * valueOfOneRemovedBurden;
        // removing my burdens good, removing opponent's burdens bad
        return player.equals(plannedBoardState.getCurrentFpPlayer()) ? totalValue : -totalValue;
    }

    @Override
    public String toString(String player, PlannedBoardState plannedBoardState) {
        int burdensPlaced = plannedBoardState.getBurdens();
        int toBeRemoved = Math.min(amount, burdensPlaced);
        if (toBeRemoved == 1) {
            return "remove a burden";
        }
        return "remove " + toBeRemoved + " burdens";
    }
}
