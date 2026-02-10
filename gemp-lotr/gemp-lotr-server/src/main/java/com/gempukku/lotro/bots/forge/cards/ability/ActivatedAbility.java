package com.gempukku.lotro.bots.forge.cards.ability;

import com.gempukku.lotro.common.Phase;

public abstract class ActivatedAbility extends Ability {
    @Override
    public AbilityType getAbilityType() {
        return AbilityType.ACTIVATED;
    }

    public abstract Phase getPhase();
}
