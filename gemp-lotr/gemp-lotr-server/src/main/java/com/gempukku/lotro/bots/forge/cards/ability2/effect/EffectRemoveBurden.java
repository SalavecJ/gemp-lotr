package com.gempukku.lotro.bots.forge.cards.ability2.effect;

import com.gempukku.lotro.logic.timing.DefaultLotroGame;

public class EffectRemoveBurden extends Effect{
    protected final int amount ;

    public EffectRemoveBurden(int amount) {
        this.amount = amount;
    }

    @Override
    public double getValueIfResolved(String player, DefaultLotroGame game) {
        int burdensPlaced = game.getGameState().getBurdens();
        int toBeRemoved = Math.min(amount, burdensPlaced);
        double valueOfOneRemovedBurden = 0.9 + ((double) burdensPlaced / 10); // more burdens placed, better to remove
        double totalValue = toBeRemoved * valueOfOneRemovedBurden;
        // removing my burdens good, removing opponent's burdens bad
        return player.equals(game.getGameState().getCurrentPlayerId()) ? totalValue : -totalValue;
    }

    @Override
    public String toString(String player, DefaultLotroGame game) {
        int burdensPlaced = game.getGameState().getBurdens();
        int toBeRemoved = Math.min(amount, burdensPlaced);
        if (toBeRemoved == 1) {
            return "remove a burden";
        }
        return "remove " + toBeRemoved + " burdens";
    }
}
