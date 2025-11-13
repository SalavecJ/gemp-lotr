package com.gempukku.lotro.bots.forge.cards.ability2.cost;

import com.gempukku.lotro.bots.forge.cards.abstractcard.BotCard;
import com.gempukku.lotro.bots.forge.plan.PlannedBoardState;

import java.util.ArrayList;
import java.util.List;

public abstract class CostWithTarget extends Cost {
    public abstract void payWithTarget(String player, PlannedBoardState plannedBoardState, BotCard target);
    public abstract double getValueIfPayedWithTarget(String player, PlannedBoardState plannedBoardState, BotCard target);
    public abstract String toString(String player, PlannedBoardState plannedBoardState, BotCard target);

    public abstract ArrayList<BotCard> getPotentialTargets(String player, PlannedBoardState plannedBoardState);

    public final BotCard chooseTarget(String player, PlannedBoardState plannedBoardState) {
        List<BotCard> potentialTargets = getPotentialTargets(player, plannedBoardState);
        if (potentialTargets.isEmpty()) {
            return null;
        } else {
            potentialTargets.sort((o1, o2) -> Double.compare(getValueIfPayedWithTarget(player, plannedBoardState, o2), getValueIfPayedWithTarget(player, plannedBoardState, o1)));
            return potentialTargets.getFirst();
        }
    }

    public final double getMaximumPossibleValue(String player, PlannedBoardState plannedBoardState) {
        BotCard bestTarget = chooseTarget(player, plannedBoardState);
        if (bestTarget == null) {
            return 0;
        } else {
            return getValueIfPayedWithTarget(player, plannedBoardState, bestTarget);
        }
    }

    public final boolean canPayCostWithTarget(String player, PlannedBoardState plannedBoardState, BotCard target) {
        return getPotentialTargets(player, plannedBoardState).contains(target);
    }


    @Override
    public final boolean canPayCost(String player, PlannedBoardState plannedBoardState) {
        return !getPotentialTargets(player, plannedBoardState).isEmpty();
    }

    @Override
    public final void pay(String player, PlannedBoardState plannedBoardState) {
        throw new IllegalStateException("Cannot pay cost without target provided");
    }
    @Override
    public final double getValueIfPayed(String player, PlannedBoardState plannedBoardState) {
        throw new IllegalStateException("Cannot get value without target provided");
    }
    @Override
    public final String toString(String player, PlannedBoardState plannedBoardState) {
        throw new IllegalStateException("Cannot produce string without target provided");
    }
}
