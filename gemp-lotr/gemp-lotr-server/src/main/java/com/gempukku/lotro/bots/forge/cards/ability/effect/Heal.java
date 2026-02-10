package com.gempukku.lotro.bots.forge.cards.ability.effect;

import com.gempukku.lotro.bots.forge.cards.ability.targeting.BotTargetingPolicy;
import com.gempukku.lotro.bots.forge.cards.ability.targeting.HealTargeting;

public class Heal extends Effect {
    private final int healAmount;

    public Heal(int healAmount) {
        this.healAmount = healAmount;
    }

    public Heal() {
        this(1);
    }

    @Override
    public BotTargetingPolicy getBotTargetingPolicy() {
        return new HealTargeting(healAmount);
    }

    @Override
    public String toString() {
        if (healAmount == 1) {
            return "Heal";
        } else {
            return "Heal " + healAmount + " wounds";
        }
    }

    @Override
    public boolean decisionTextMatches(String decisionText) {
        return decisionText.equals("Choose cards to heal");
    }
}
