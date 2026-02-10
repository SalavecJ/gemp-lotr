package com.gempukku.lotro.bots.forge.cards.ability.effect;

import com.gempukku.lotro.bots.forge.cards.ability.targeting.BotTargetingPolicy;
import com.gempukku.lotro.bots.forge.cards.ability.targeting.LowestValueInHandTargeting;

public class PutCardsFromHandOnBottomOfDeck extends Effect {
    private final int count;

    public PutCardsFromHandOnBottomOfDeck(int count) {
        this.count = count;
    }

    public PutCardsFromHandOnBottomOfDeck() {
        this(1);
    }

    @Override
    public BotTargetingPolicy getBotTargetingPolicy() {
        return new LowestValueInHandTargeting();
    }

    @Override
    public String toString() {
        if (count == 1) {
            return "Put a card from hand on bottom of deck";
        } else {
            return "Put " + count + " cards from hand on bottom of deck";
        }
    }

    @Override
    public boolean decisionTextMatches(String decisionText) {
        return decisionText.equals("Choose cards from hand");
    }
}
