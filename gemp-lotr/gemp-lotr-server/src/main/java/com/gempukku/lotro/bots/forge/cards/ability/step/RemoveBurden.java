package com.gempukku.lotro.bots.forge.cards.ability.step;

import com.gempukku.lotro.bots.forge.cards.ability.AbilityStep;
import com.gempukku.lotro.bots.forge.cards.ability.targeting.BotTargetingPolicy;
import com.gempukku.lotro.bots.forge.utils.BurdensValueUtil;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

public class RemoveBurden extends AbilityStep {
    private final int amount;

    public RemoveBurden(int amount) {
        this.amount = amount;
    }

    public RemoveBurden() {
        this(1);
    }

    @Override
    public BotTargetingPolicy getBotTargetingPolicy() {
        return null;
    }

    @Override
    public String toString() {
        if (amount == 1) {
            return "Remove a burden";
        } else {
            return "Remove " + amount + " burdens";
        }
    }

    @Override
    public boolean decisionTextMatches(String decisionText) {
        return false;
    }

    @Override
    public double getValue(DefaultLotroGame game, String playerName) {
        return BurdensValueUtil.evaluateBurdenChangeValue(playerName, game, -amount);
    }
}
