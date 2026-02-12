package com.gempukku.lotro.bots.forge.cards.ability;

import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

public abstract class ActivatedAbility extends Ability {
    @Override
    public AbilityType getAbilityType() {
        return AbilityType.ACTIVATED;
    }

    public abstract Timeword getTimeword();

    public double valueIfActivated(DefaultLotroGame game, String playerName) {
        double value = 0;
        for (AbilityStep step : getSteps()) {
            value += step.getValue(game, playerName);
        }
        return value;
    }
}
