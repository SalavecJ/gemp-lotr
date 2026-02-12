package com.gempukku.lotro.bots.forge.cards.ability.targeting;

import com.gempukku.lotro.bots.forge.cards.abstractcards.BotCard;
import com.gempukku.lotro.bots.forge.utils.WoundsValueUtil;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.ArrayList;
import java.util.List;

public class ExertTargeting extends BotTargetingPolicy {
    private final int amount;

    public ExertTargeting(int amount) {
        this.amount = amount;
    }

    @Override
    public List<BotCard> getTargets(List<List<BotCard>> possibleTargets, DefaultLotroGame game, String playerName) {
        List<Double> values = new ArrayList<>();
        for (List<BotCard> targets : possibleTargets) {
            double value = 0.0;
            for (BotCard target : targets) {
                value += WoundsValueUtil.evaluateWoundsChangeValue(playerName, game, target.getPhysicalCard(), amount);
            }
            values.add(value);
        }

        double maxValue = values.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        int bestIndex = values.indexOf(maxValue);
        if (bestIndex == -1) {
            bestIndex = 0; // Fallback to the first option if something goes wrong
        }
        return possibleTargets.get(bestIndex);
    }

    @Override
    public String toString() {
        return "Choose card with with highest exert value";
    }
}
