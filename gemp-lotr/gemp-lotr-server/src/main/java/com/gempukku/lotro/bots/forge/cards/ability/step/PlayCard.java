package com.gempukku.lotro.bots.forge.cards.ability.step;

import com.gempukku.lotro.bots.forge.cards.ability.AbilityStep;
import com.gempukku.lotro.bots.forge.cards.ability.targeting.BotTargetingPolicy;
import com.gempukku.lotro.bots.forge.cards.abstractcards.BotCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.function.Predicate;

public class PlayCard extends AbilityStep {
    private final Predicate<BotCard> cardFilter;
    private final int twilightModifier;

    public PlayCard(Predicate<BotCard> cardFilter, int twilightModifier) {
        this.cardFilter = cardFilter;
        this.twilightModifier = twilightModifier;
    }

    public PlayCard(Predicate<BotCard> cardFilter) {
        this(cardFilter, 0);
    }

    public boolean canPlayCard(BotCard card) {
        return cardFilter.test(card);
    }

    @Override
    public BotTargetingPolicy getBotTargetingPolicy() {
        return null;
    }

    @Override
    public String toString() {
        if (twilightModifier == 0) {
            return "Play card from hand";
        } else {
            return "Play card from hand with twilight modifier " + twilightModifier;
        }
    }

    @Override
    public boolean decisionTextMatches(String decisionText) {
        return decisionText.equals("Choose card to play from hand");
    }

    @Override
    public double getValue(DefaultLotroGame game, String playerName) {
        return -twilightModifier * 0.6;
    }
}
