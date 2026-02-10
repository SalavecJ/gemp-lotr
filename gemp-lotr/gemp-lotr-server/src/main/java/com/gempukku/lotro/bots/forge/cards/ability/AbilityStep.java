package com.gempukku.lotro.bots.forge.cards.ability;

import com.gempukku.lotro.bots.forge.cards.ability.targeting.BotTargetingPolicy;

public abstract class AbilityStep {
    public abstract AbilityStepType getType();

    public abstract BotTargetingPolicy getBotTargetingPolicy();

    public abstract String toString();

    public abstract boolean decisionTextMatches(String decisionText);
}
