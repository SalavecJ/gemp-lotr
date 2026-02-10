package com.gempukku.lotro.bots.forge.cards.ability.effect;

import com.gempukku.lotro.bots.forge.cards.ability.targeting.BotTargetingPolicy;
import com.gempukku.lotro.bots.forge.cards.ability.targeting.HighestValueInHandTargeting;

public class PutCardsFromDiscardIntoHand extends Effect {
    private final int count;

    public PutCardsFromDiscardIntoHand(int count) {
        this.count = count;
    }

    public PutCardsFromDiscardIntoHand() {
        this(1);
    }

    @Override
    public BotTargetingPolicy getBotTargetingPolicy() {
        return new HighestValueInHandTargeting();
    }

    @Override
    public String toString() {
        if (count == 1) {
            return "Put a card from discard into hand";
        } else {
            return "Put " + count + " cards from discard into hand";
        }
    }

    @Override
    public boolean decisionTextMatches(String decisionText) {
        return decisionText.equals("Choose card from discard");
    }
}
