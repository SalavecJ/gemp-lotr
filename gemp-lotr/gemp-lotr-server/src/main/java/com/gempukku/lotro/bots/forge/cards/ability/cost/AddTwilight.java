package com.gempukku.lotro.bots.forge.cards.ability.cost;

import com.gempukku.lotro.bots.forge.cards.ability.targeting.BotTargetingPolicy;

public class AddTwilight extends Cost {
    private final int twilightAmount;

    public AddTwilight(int twilightAmount) {
        this.twilightAmount = twilightAmount;
    }

    @Override
    public BotTargetingPolicy getBotTargetingPolicy() {
        return null;
    }

    @Override
    public String toString() {
        return "Add " + twilightAmount + " twilight";
    }

    @Override
    public boolean decisionTextMatches(String decisionText) {
        return false;
    }
}
