package com.gempukku.lotro.bots.forge.cards.ability2.effect;

import com.gempukku.lotro.logic.timing.DefaultLotroGame;

public class EffectAddTwilight extends Effect{
    protected final int amount ;

    public EffectAddTwilight(int amount) {
        this.amount = amount;
    }

    @Override
    public double getValueIfResolved(String player, DefaultLotroGame game) {
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
