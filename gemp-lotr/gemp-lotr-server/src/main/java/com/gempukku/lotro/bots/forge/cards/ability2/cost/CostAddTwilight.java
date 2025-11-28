package com.gempukku.lotro.bots.forge.cards.ability2.cost;

import com.gempukku.lotro.logic.timing.DefaultLotroGame;

public class CostAddTwilight extends Cost {
    protected final int amount;

    public CostAddTwilight(int amount) {
        this.amount = amount;
    }


    @Override
    public boolean canPayCost(String player, DefaultLotroGame game) {
        return true;
    }

    @Override
    public double getValueIfPayed(String player, DefaultLotroGame game) {
        double returnValue = (double) amount * 0.4;
        if (player.equals(game.getGameState().getCurrentPlayerId())) {
            returnValue *= -1;
        }

        return returnValue;
    }

    @Override
    public String toString(String player, DefaultLotroGame game) {
        return "add " + amount + " twilight";
    }
}
