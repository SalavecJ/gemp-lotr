package com.gempukku.lotro.bots.forge.cards.ability.targeting;

import com.gempukku.lotro.bots.forge.cards.abstractcards.BotCard;
import com.gempukku.lotro.bots.forge.utils.HandValueUtil;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.ArrayList;
import java.util.List;

public class LowestValueInHandTargeting extends BotTargetingPolicy {
    @Override
    public List<BotCard> getTargets(List<List<BotCard>> possibleTargets, DefaultLotroGame game, String playerName) {
        List<Double> values = new ArrayList<>();
        for (List<BotCard> targets : possibleTargets) {
            double value = 0.0;
            for (BotCard target : targets) {
                value += HandValueUtil.getHandValue(target, game);
            }
            values.add(value);
        }

        double minValue = values.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
        int bestIndex = values.indexOf(minValue);
        if (bestIndex == -1) {
            bestIndex = 0; // Fallback to the first option if something goes wrong
        }
        return possibleTargets.get(bestIndex);
    }

    @Override
    public String toString() {
        return "Choose cards with lowest value in hand";
    }
}
