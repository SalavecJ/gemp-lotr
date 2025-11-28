package com.gempukku.lotro.bots.forge.cards.ability2.cost;


import com.gempukku.lotro.logic.timing.DefaultLotroGame;

public class CostRemoveTwilight extends Cost {
    protected final int amount;

    public CostRemoveTwilight(int amount) {
        this.amount = amount;
    }

    @Override
    public boolean canPayCost(String player, DefaultLotroGame game) {
        return game.getGameState().getTwilightPool() >= amount;
    }

    @Override
    public double getValueIfPayed(String player, DefaultLotroGame game) {
        double returnValue = (double) amount * 0.4;
        if (player.equals(game.getGameState().getCurrentShadowPlayer())) {
            returnValue *= -1;
        }

        return returnValue;
    }

    @Override
    public String toString(String player, DefaultLotroGame game) {
        return "remove " + amount + " twilight";
    }
}
