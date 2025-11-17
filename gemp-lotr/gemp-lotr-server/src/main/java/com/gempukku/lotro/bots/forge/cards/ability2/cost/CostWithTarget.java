package com.gempukku.lotro.bots.forge.cards.ability2.cost;

import com.gempukku.lotro.bots.forge.cards.abstractcard.BotCard;
import com.gempukku.lotro.bots.forge.plan.PlannedBoardState;

import java.util.ArrayList;
import java.util.List;

public abstract class CostWithTarget extends Cost {
    public abstract int getNumberOfTargetsRequired();

    public final void payWithTargets(String player, PlannedBoardState plannedBoardState, List<BotCard> targets) {
        if (targets == null || targets.size() != getNumberOfTargetsRequired()) {
            return;
        } else {
            for (BotCard target : targets) {
                payWith(player, plannedBoardState, target);
            }
        }
    }
    protected abstract void payWith(String player, PlannedBoardState plannedBoardState, BotCard target);

    public final double getValueIfPayedWithTargets(String player, PlannedBoardState plannedBoardState, List<BotCard> targets) {
        if (targets == null || targets.size() != getNumberOfTargetsRequired()) {
            return 0;
        } else {
            return targets.stream().mapToDouble(target -> getValueIfPayedWith(player, plannedBoardState, target)).sum();
        }
    }
    protected abstract double getValueIfPayedWith(String player, PlannedBoardState plannedBoardState, BotCard target);
    public abstract String toString(String player, PlannedBoardState plannedBoardState, List<BotCard> targets);

    public abstract ArrayList<BotCard> getPotentialTargets(String player, PlannedBoardState plannedBoardState);

    public final List<BotCard> chooseTargets(String player, PlannedBoardState plannedBoardState) {
        List<BotCard> potentialTargets = getPotentialTargets(player, plannedBoardState);
        if (potentialTargets.size() < getNumberOfTargetsRequired()) {
            return null;
        } else {
            potentialTargets.sort((o1, o2) -> Double.compare(getValueIfPayedWith(player, plannedBoardState, o2), getValueIfPayedWith(player, plannedBoardState, o1)));
            return potentialTargets.subList(0, getNumberOfTargetsRequired());
        }
    }

    public final List<BotCard> chooseTargets(String player, PlannedBoardState plannedBoardState, List<List<BotCard>> targets) {
        if (targets == null) {
            return null;
        }
        List<List<BotCard>> potentialCombinations = new ArrayList<>(targets);
        return potentialCombinations.stream().min((o1, o2) -> Double.compare(
                getValueIfPayedWithTargets(player, plannedBoardState, o2),
                getValueIfPayedWithTargets(player, plannedBoardState, o1))).orElse(null);
    }

    public final double getMaximumPossibleValue(String player, PlannedBoardState plannedBoardState) {
        List<BotCard> bestTargets = chooseTargets(player, plannedBoardState);
        if (bestTargets == null) {
            return 0;
        } else {
            return getValueIfPayedWithTargets(player, plannedBoardState, bestTargets);
        }
    }

    public final boolean canPayCostWithTarget(String player, PlannedBoardState plannedBoardState, BotCard target) {
        return getPotentialTargets(player, plannedBoardState).contains(target);
    }


    @Override
    public final boolean canPayCost(String player, PlannedBoardState plannedBoardState) {
        return getPotentialTargets(player, plannedBoardState).size() >= getNumberOfTargetsRequired();
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
