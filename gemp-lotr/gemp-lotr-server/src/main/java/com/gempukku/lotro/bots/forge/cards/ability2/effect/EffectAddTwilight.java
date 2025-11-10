package com.gempukku.lotro.bots.forge.cards.ability2.effect;

import com.gempukku.lotro.bots.forge.plan.PlannedBoardState;

public class EffectAddTwilight extends Effect{
    protected final int amount ;

    public EffectAddTwilight(int amount) {
        this.amount = amount;
    }

    @Override
    public void resolve(String player, PlannedBoardState plannedBoardState) {
        plannedBoardState.addTwilight(amount);
    }

    @Override
    public double getValueIfResolved(String player, PlannedBoardState plannedBoardState) {
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
