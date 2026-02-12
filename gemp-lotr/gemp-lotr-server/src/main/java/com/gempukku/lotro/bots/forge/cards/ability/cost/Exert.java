package com.gempukku.lotro.bots.forge.cards.ability.cost;

import com.gempukku.lotro.bots.forge.cards.BotCardFactory;
import com.gempukku.lotro.bots.forge.cards.ability.targeting.BotTargetingPolicy;
import com.gempukku.lotro.bots.forge.cards.ability.targeting.ExertTargeting;
import com.gempukku.lotro.bots.forge.cards.abstractcards.BotCard;
import com.gempukku.lotro.bots.forge.utils.WoundsValueUtil;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.List;
import java.util.function.Predicate;

public class Exert extends Cost {
    private final int count;
    private final Predicate<BotCard> cardFilter;

    public Exert(Predicate<BotCard> cardFilter, int count) {
        this.cardFilter = cardFilter;
        this.count = count;
    }

    public Exert(Predicate<BotCard> cardFilter) {
        this(cardFilter, 1);
    }

    @Override
    public BotTargetingPolicy getBotTargetingPolicy() {
        return new ExertTargeting(count);
    }

    @Override
    public String toString() {
        if (count == 1) {
            return "Exert";
        }
        return "Exert " + count + " times";
    }

    @Override
    public boolean decisionTextMatches(String decisionText) {
        return decisionText.equals("Choose cards to exert");
    }

    @Override
    public double getValue(DefaultLotroGame game, String playerName) {
        List<BotCard> potentialTargets = game.getGameState().getActiveCards().stream()
                .map(BotCardFactory::create)
                .filter(cardFilter)
                .filter(card -> game.getModifiersQuerying().getVitality(game, card.getPhysicalCard()) > 1)
                .toList();

        if (potentialTargets.isEmpty()) {
            return 0;
        }

        List<List<BotCard>> tagetingFormatted = potentialTargets.stream()
                .map(List::of)
                .toList();

        BotCard chosenTarget = getBotTargetingPolicy().getTargets(tagetingFormatted, game, playerName).getFirst();

        return WoundsValueUtil.evaluateWoundsChangeValue(playerName, game, chosenTarget.getPhysicalCard(), count);
    }
}
