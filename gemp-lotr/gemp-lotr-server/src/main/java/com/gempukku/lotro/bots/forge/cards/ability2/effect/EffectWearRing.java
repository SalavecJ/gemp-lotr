package com.gempukku.lotro.bots.forge.cards.ability2.effect;

import com.gempukku.lotro.logic.timing.DefaultLotroGame;

public class EffectWearRing extends Effect {

    public EffectWearRing() {

    }

    @Override
    public double getValueIfResolved(String player, DefaultLotroGame game) {
        return -0.1; // wearing is generally bad
    }

    @Override
    public String toString(String player, DefaultLotroGame game) {
        return "wear the One Ring";
    }
}
