package com.gempukku.lotro.bots.forge.cards.ability.cost;

import com.gempukku.lotro.bots.forge.cards.ability.targeting.BotTargetingPolicy;

public class ExertSelf extends Cost {
    private final int count;

    public ExertSelf(int count) {
        this.count = count;
    }

    public ExertSelf() {
        this(1);
    }

    @Override
    public BotTargetingPolicy getBotTargetingPolicy() {
        return null;
    }

    @Override
    public String toString() {
        if (count == 1) {
            return "Exert self";
        }
        return "Exert self " + count + " times";
    }

    @Override
    public boolean decisionTextMatches(String decisionText) {
        return false;
    }
}
