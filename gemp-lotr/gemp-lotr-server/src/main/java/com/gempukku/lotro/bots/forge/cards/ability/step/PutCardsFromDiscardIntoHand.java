package com.gempukku.lotro.bots.forge.cards.ability.step;

import com.gempukku.lotro.bots.forge.cards.BotCardFactory;
import com.gempukku.lotro.bots.forge.cards.ability.AbilityStep;
import com.gempukku.lotro.bots.forge.cards.ability.targeting.BotTargetingPolicy;
import com.gempukku.lotro.bots.forge.cards.ability.targeting.HighestValueInHandTargeting;
import com.gempukku.lotro.bots.forge.cards.abstractcards.BotCard;
import com.gempukku.lotro.bots.forge.utils.HandValueUtil;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class PutCardsFromDiscardIntoHand extends AbilityStep {
    private final int count;
    private final Predicate<BotCard> cardFilter;

    public PutCardsFromDiscardIntoHand(Predicate<BotCard> cardFilter, int count) {
        this.cardFilter = cardFilter;
        this.count = count;
    }

    public PutCardsFromDiscardIntoHand(Predicate<BotCard> cardFilter) {
        this(cardFilter, 1);
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

    @Override
    public double getValue(DefaultLotroGame game, String playerName) {
        List<BotCard> cardsInDiscard = new ArrayList<>(game.getGameState().getDiscard(playerName).stream()
                .map((Function<PhysicalCard, BotCard>) BotCardFactory::create)
                .filter(cardFilter)
                .toList());
        cardsInDiscard.sort(Comparator.comparingDouble(o -> -HandValueUtil.getHandValue(o, game))); // sort from best to worst card in discard, thats why 'minus' in comparator
        double totalHandValue = 0;
        for (int i = 0; i < Math.min(count, cardsInDiscard.size()); i++) {
            totalHandValue += HandValueUtil.getHandValue(cardsInDiscard.get(i), game);
        }
        if (totalHandValue == 0) {
            return 0;
        } else if (totalHandValue > 0) { // return of good cards
            return Math.min(count, cardsInDiscard.size());
        } else { // clogging hand with bad cards
            return -1;
        }
    }
}
