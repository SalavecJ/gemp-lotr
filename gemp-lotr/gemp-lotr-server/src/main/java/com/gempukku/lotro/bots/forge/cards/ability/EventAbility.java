package com.gempukku.lotro.bots.forge.cards.ability;

public abstract class EventAbility extends Ability {
    @Override
    public AbilityType getAbilityType() {
        return AbilityType.EVENT;
    }
}
