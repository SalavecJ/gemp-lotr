package com.gempukku.lotro.bots.forge.cards.ability.cost;

import com.gempukku.lotro.bots.forge.cards.ability.AbilityStep;
import com.gempukku.lotro.bots.forge.cards.ability.AbilityStepType;

public abstract class Cost extends AbilityStep {
    @Override
    public AbilityStepType getType() {
        return AbilityStepType.COST;
    }
}
