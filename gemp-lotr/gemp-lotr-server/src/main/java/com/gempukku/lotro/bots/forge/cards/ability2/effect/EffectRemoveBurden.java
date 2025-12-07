package com.gempukku.lotro.bots.forge.cards.ability2.effect;

import com.gempukku.lotro.bots.forge.cards.ability2.util.BurdensValueUtil;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

public class EffectRemoveBurden extends Effect{
    protected final int amount ;

    public EffectRemoveBurden(int amount) {
        this.amount = amount;
    }

    @Override
    public double getValueIfResolved(String player, DefaultLotroGame game) {
        // Use BurdensValueUtil for consistent burden evaluation
        // Negative amount because we're removing burdens
        return BurdensValueUtil.evaluateBurdenChangeValue(player, game, -amount);
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
