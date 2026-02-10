package com.gempukku.lotro.bots.forge.cards.ability;

import com.gempukku.lotro.common.Phase;

import java.util.List;

public abstract class ActivatedAbility extends Ability {
    @Override
    public AbilityType getAbilityType() {
        return AbilityType.ACTIVATED;
    }

    public abstract Phase getPhase();

    public abstract List<AbilityStep> getSteps();
}
