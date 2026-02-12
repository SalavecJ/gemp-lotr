package com.gempukku.lotro.bots.forge.cards.ability.effect;

import com.gempukku.lotro.bots.forge.cards.BotCardFactory;
import com.gempukku.lotro.bots.forge.cards.ability.targeting.BotTargetingPolicy;
import com.gempukku.lotro.bots.forge.cards.abstractcards.BotCard;
import com.gempukku.lotro.bots.forge.utils.DiscardValueUtil;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.List;
import java.util.function.Predicate;

public class DiscardAll extends Effect {
    private final Predicate<BotCard> cardFilter;

    public DiscardAll(Predicate<BotCard> cardFilter) {
        this.cardFilter = cardFilter;
    }

    @Override
    public BotTargetingPolicy getBotTargetingPolicy() {
        return null;
    }

    @Override
    public String toString() {
        return "Discard from play all";
    }

    @Override
    public boolean decisionTextMatches(String decisionText) {
        return false;
    }

    @Override
    public double getValue(DefaultLotroGame game, String playerName) {
        List<BotCard> affectedCards = game.getGameState().getActiveCards().stream()
                .map(BotCardFactory::create)
                .filter(cardFilter)
                .toList();

        return affectedCards.stream().mapToDouble(value -> DiscardValueUtil.getDiscardValue(value, game, playerName)).sum();
    }
}
