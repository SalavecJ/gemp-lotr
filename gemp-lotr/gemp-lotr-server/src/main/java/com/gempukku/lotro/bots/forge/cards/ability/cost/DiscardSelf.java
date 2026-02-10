package com.gempukku.lotro.bots.forge.cards.ability.cost;

import com.gempukku.lotro.bots.forge.cards.ability.targeting.BotTargetingPolicy;

public class DiscardSelf extends Cost {
    @Override
    public BotTargetingPolicy getBotTargetingPolicy() {
        return null;
    }

    @Override
    public String toString() {
        return "Discard self from play";
    }

    @Override
    public boolean decisionTextMatches(String decisionText) {
        return false;
    }
}
