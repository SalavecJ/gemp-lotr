package com.gempukku.lotro.bots.forge.cards.ability2.cost;

import com.gempukku.lotro.bots.forge.plan.PlannedBoardState;

public class CostAddTwilight extends Cost {
    protected final int amount;

    public CostAddTwilight(int amount) {
        this.amount = amount;
    }

    @Override
    public void pay(String player, PlannedBoardState plannedBoardState) {
        plannedBoardState.addTwilight(amount);
    }

    @Override
    public boolean canPayCost(String player, PlannedBoardState plannedBoardState) {
        return true;
    }

    @Override
    public double getValueIfPayed(String player, PlannedBoardState plannedBoardState) {
        double returnValue = (double) amount * 0.4;
        if (player.equals(plannedBoardState.getCurrentFpPlayer())) {
            returnValue *= -1;
        }

        return returnValue;
    }

    @Override
    public String toString(String player, PlannedBoardState plannedBoardState) {
        return "add " + amount + " twilight";
    }
}
