package com.gempukku.lotro.bots.forge.cards.ability;

import java.util.List;

public abstract class Ability {
    public abstract AbilityType getAbilityType();

    public abstract List<AbilityStep> getSteps();
}
