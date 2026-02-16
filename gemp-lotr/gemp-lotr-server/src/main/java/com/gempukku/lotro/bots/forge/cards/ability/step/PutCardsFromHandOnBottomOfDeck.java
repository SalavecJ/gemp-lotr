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

public class PutCardsFromHandOnBottomOfDeck extends AbilityStep {
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

    @Override
    public double getValue(DefaultLotroGame game, String playerName) {
        List<BotCard> cardsInHand = new ArrayList<>(game.getGameState().getHand(playerName).stream().map((Function<PhysicalCard, BotCard>) BotCardFactory::create).toList());
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
