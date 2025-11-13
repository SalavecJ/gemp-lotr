package com.gempukku.lotro.bots.forge.cards.ability2.cost;

import com.gempukku.lotro.bots.forge.cards.ability2.util.WoundsValueUtil;
import com.gempukku.lotro.bots.forge.cards.abstractcard.BotCard;
import com.gempukku.lotro.bots.forge.plan.PlannedBoardState;

import java.util.ArrayList;
import java.util.function.Predicate;

public class CostExert extends CostWithTarget {
    protected final Predicate<BotCard> targetPredicate;
    protected final int amount;

    public CostExert(Predicate<BotCard> targetPredicate, int amount) {
        this.targetPredicate = targetPredicate;
        this.amount = amount;
    }

    @Override
    public ArrayList<BotCard> getPotentialTargets(String player, PlannedBoardState plannedBoardState) {
        return new ArrayList<>(plannedBoardState.getActiveCards().stream()
                .filter(targetPredicate)
                .filter(botCard -> plannedBoardState.getVitality(botCard) > amount)
                .toList());
    }

    @Override
    public String toString(String player, PlannedBoardState plannedBoardState, BotCard target) {
        if (amount == 1) {
            return "exert " + target.getSelf().getBlueprint().getFullName();
        } else {

            return "exert " + amount + " times " + target.getSelf().getBlueprint().getFullName();
        }
    }

    @Override
    public void payWithTarget(String player, PlannedBoardState plannedBoardState, BotCard target) {
        if (!canPayCostWithTarget(player, plannedBoardState, target)) {
            throw new IllegalStateException("Cost cannot be payed");
        }
        plannedBoardState.exert(target, amount);
    }

    @Override
    public double getValueIfPayedWithTarget(String player, PlannedBoardState plannedBoardState, BotCard target) {
        if (!canPayCostWithTarget(player, plannedBoardState, target)) {
            throw new IllegalStateException("Cost cannot be payed");
        }

        int vitality = plannedBoardState.getVitality(target);
        return WoundsValueUtil.evaluateWoundsChangeValue(player, plannedBoardState, target, Math.min(amount, vitality - 1));
    }
}
