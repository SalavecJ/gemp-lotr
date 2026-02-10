package com.gempukku.lotro.bots.forge.cards.ability.effect;

import com.gempukku.lotro.bots.forge.cards.ability.AbilityStep;
import com.gempukku.lotro.bots.forge.cards.ability.AbilityStepType;

public abstract class Effect extends AbilityStep {
    @Override
    public AbilityStepType getType() {
        return AbilityStepType.EFFECT;
    }
}
