package com.gempukku.lotro.bots.forge.cards.ability2.effect;

import com.gempukku.lotro.logic.timing.DefaultLotroGame;

public class EffectMillOpponent extends Effect {
    private final int amount;

    public EffectMillOpponent(int amount) {
        this.amount = amount;
    }

    @Override
    public double getValueIfResolved(String player, DefaultLotroGame game) {
        return amount * 0.2;
    }

    @Override
    public String toString(String player, DefaultLotroGame game) {
        if (amount == 1) {
            return "mill a card from opponent's deck";
        }
        return "mill " + amount + " cards from opponent's deck";
    }
}
