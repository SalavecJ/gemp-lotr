package com.gempukku.lotro.bots.forge.cards.ability.cost;

import com.gempukku.lotro.bots.forge.cards.ability.targeting.BotTargetingPolicy;
import com.gempukku.lotro.bots.forge.cards.ability.targeting.LowestValueInHandTargeting;

public class DiscardCardsFromHand extends Cost {
    private final int count;

    public DiscardCardsFromHand(int count) {
        this.count = count;
    }

    public DiscardCardsFromHand() {
        this(1);
    }

    @Override
    public BotTargetingPolicy getBotTargetingPolicy() {
        return new LowestValueInHandTargeting();
    }

    @Override
    public String toString() {
        if (count == 1) {
            return "Discard card from hand";
        } else {
            return "Discard " + count + " cards from hand";
        }
    }

    @Override
    public boolean decisionTextMatches(String decisionText) {
        return decisionText.equals("Choose cards from hand to discard");
    }
}
