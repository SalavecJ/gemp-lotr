package com.gempukku.lotro.bots.forge.cards.ability.step;

import com.gempukku.lotro.bots.forge.cards.BotCardFactory;
import com.gempukku.lotro.bots.forge.cards.ability.AbilityStep;
import com.gempukku.lotro.bots.forge.cards.ability.targeting.BotTargetingPolicy;
import com.gempukku.lotro.bots.forge.cards.ability.targeting.HealTargeting;
import com.gempukku.lotro.bots.forge.cards.abstractcards.BotCard;
import com.gempukku.lotro.bots.forge.utils.WoundsValueUtil;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.List;
import java.util.function.Predicate;

public class Heal extends AbilityStep {
    private final int healAmount;
    private final Predicate<BotCard> cardFilter;

    public Heal(Predicate<BotCard> cardFilter, int healAmount) {
        this.cardFilter = cardFilter;
        this.healAmount = healAmount;
    }

    public Heal(Predicate<BotCard> cardFilter) {
        this(cardFilter, 1);
    }

    @Override
    public BotTargetingPolicy getBotTargetingPolicy() {
        return new HealTargeting(healAmount);
    }

    @Override
    public String toString() {
        if (healAmount == 1) {
            return "Heal";
        } else {
            return "Heal " + healAmount + " wounds";
        }
    }

    @Override
    public boolean decisionTextMatches(String decisionText) {
        return decisionText.equals("Choose cards to heal");
    }

    @Override
    public double getValue(DefaultLotroGame game, String playerName) {
        List<BotCard> potentialTargets = game.getGameState().getActiveCards().stream()
                .map(BotCardFactory::create)
                .filter(cardFilter)
                .toList();

        if (potentialTargets.isEmpty()) {
            return 0;
        }

        List<List<BotCard>> tagetingFormatted = potentialTargets.stream()
                .map(List::of)
                .toList();

        BotCard chosenTarget = getBotTargetingPolicy().getTargets(tagetingFormatted, game, playerName).getFirst();

        return WoundsValueUtil.evaluateWoundsChangeValue(playerName, game, chosenTarget.getPhysicalCard(), -healAmount);
    }
}
