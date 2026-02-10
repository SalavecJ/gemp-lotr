package com.gempukku.lotro.bots.forge.cards.ability;

public abstract class AbilityStep {
    public abstract AbilityStepType getType();

    public abstract BotTargetingPolicy getBotTargetingPolicy();

    public abstract String toString();
}
