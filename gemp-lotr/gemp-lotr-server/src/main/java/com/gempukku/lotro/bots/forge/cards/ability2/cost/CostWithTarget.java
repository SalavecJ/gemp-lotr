package com.gempukku.lotro.bots.forge.cards.ability2.cost;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.ArrayList;
import java.util.List;

public abstract class CostWithTarget extends Cost {
    public abstract int getNumberOfTargetsRequired();
    public abstract boolean decisionTextMatches(String decisionText);

    public final double getValueIfPayedWithTargets(String player, DefaultLotroGame game, List<PhysicalCard> targets) {
        if (targets == null || targets.size() != getNumberOfTargetsRequired()) {
            return 0;
        } else {
            return targets.stream().mapToDouble(target -> getValueIfPayedWith(player, game, target)).sum();
        }
    }

    protected abstract double getValueIfPayedWith(String player, DefaultLotroGame game, PhysicalCard target);
    public abstract String toString(String player, DefaultLotroGame game, List<PhysicalCard> targets);

    public abstract ArrayList<PhysicalCard> getPotentialTargets(String player, DefaultLotroGame game);

    public final List<PhysicalCard> chooseTargets(String player, DefaultLotroGame game) {
        List<PhysicalCard> potentialTargets = getPotentialTargets(player, game);
        if (potentialTargets.size() < getNumberOfTargetsRequired()) {
            return null;
        } else {
            potentialTargets.sort((o1, o2) -> Double.compare(getValueIfPayedWith(player, game, o2), getValueIfPayedWith(player, game, o1)));
            return potentialTargets.subList(0, getNumberOfTargetsRequired());
        }
    }

    public final List<PhysicalCard> chooseTargets(String player, DefaultLotroGame game, List<List<PhysicalCard>> targets) {
        if (targets == null) {
            return null;
        }
        List<List<PhysicalCard>> potentialCombinations = new ArrayList<>(targets);
        return potentialCombinations.stream().min((o1, o2) -> Double.compare(
                getValueIfPayedWithTargets(player, game, o2),
                getValueIfPayedWithTargets(player, game, o1))).orElse(null);
    }

    public final double getMaximumPossibleValue(String player, DefaultLotroGame game) {
        List<PhysicalCard> bestTargets = chooseTargets(player, game);
        if (bestTargets == null) {
            return 0;
        } else {
            return getValueIfPayedWithTargets(player, game, bestTargets);
        }
    }

    public final boolean canPayCostWithTarget(String player, DefaultLotroGame game, PhysicalCard target) {
        return getPotentialTargets(player, game).contains(target);
    }


    @Override
    public final boolean canPayCost(String player, DefaultLotroGame game) {
        return getPotentialTargets(player, game).size() >= getNumberOfTargetsRequired();
    }

    @Override
    public final double getValueIfPayed(String player, DefaultLotroGame game) {
        throw new IllegalStateException("Cannot get value without target provided");
    }
    @Override
    public final String toString(String player, DefaultLotroGame game) {
        throw new IllegalStateException("Cannot produce string without target provided");
    }
}
