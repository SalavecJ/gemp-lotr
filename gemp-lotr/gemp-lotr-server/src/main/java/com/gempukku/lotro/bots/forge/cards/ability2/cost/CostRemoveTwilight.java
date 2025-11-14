package com.gempukku.lotro.bots.forge.cards.ability2.cost;

import com.gempukku.lotro.bots.forge.plan.PlannedBoardState;

public class CostRemoveTwilight extends Cost {
    protected final int amount;

    public CostRemoveTwilight(int amount) {
        this.amount = amount;
    }

    @Override
    public void pay(String player, PlannedBoardState plannedBoardState) {
        plannedBoardState.removeTwilight(amount);
    }

    @Override
    public boolean canPayCost(String player, PlannedBoardState plannedBoardState) {
        return plannedBoardState.getTwilight() >= amount;
    }

    @Override
    public double getValueIfPayed(String player, PlannedBoardState plannedBoardState) {
        double returnValue = (double) amount * 0.4;
        if (player.equals(plannedBoardState.getCurrentShadowPlayer())) {
            returnValue *= -1;
        }

        return returnValue;
    }

    @Override
    public String toString(String player, PlannedBoardState plannedBoardState) {
        return "remove " + amount + " twilight";
    }
}
