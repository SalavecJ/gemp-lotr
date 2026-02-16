package com.gempukku.lotro.bots.forge.cards.ability.step;

import com.gempukku.lotro.bots.forge.cards.BotCardFactory;
import com.gempukku.lotro.bots.forge.cards.ability.AbilityStep;
import com.gempukku.lotro.bots.forge.cards.ability.targeting.BotTargetingPolicy;
import com.gempukku.lotro.bots.forge.cards.ability.targeting.LowestValueInHandTargeting;
import com.gempukku.lotro.bots.forge.cards.abstractcards.BotCard;
import com.gempukku.lotro.bots.forge.utils.HandValueUtil;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class DiscardCardsFromHand extends AbilityStep {
    private final int count;
    private final Predicate<BotCard> cardFilter;

    public DiscardCardsFromHand(Predicate<BotCard> cardFilter, int count) {
        this.cardFilter = cardFilter;
        this.count = count;
    }

    public DiscardCardsFromHand(Predicate<BotCard> cardFilter) {
        this(cardFilter, 1);
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

    @Override
    public double getValue(DefaultLotroGame game, String playerName) {
        List<BotCard> cardsInHand = new ArrayList<>(game.getGameState().getHand(playerName).stream()
                .map((Function<PhysicalCard, BotCard>) BotCardFactory::create)
                .filter(cardFilter)
                .toList());
        cardsInHand.sort(Comparator.comparingDouble(o -> HandValueUtil.getHandValue(o, game)));
        double totalHandValue = 0;
        for (int i = 0; i < Math.min(count, cardsInHand.size()); i++) {
            totalHandValue += HandValueUtil.getHandValue(cardsInHand.get(i), game);
        }
        if (totalHandValue == 0) {
            return 0;
        } else if (totalHandValue > 0) { // bad to put good cards on bottom of deck
            return -1;
        } else { // good to put bad cards on bottom of deck
            return 1;
        }
    }
}
