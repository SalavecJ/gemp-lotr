package com.gempukku.lotro.bots.forge.cards.ability2.effect;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.ArrayList;
import java.util.List;

public abstract class EffectWithTarget extends Effect {
    public abstract boolean decisionTextMatches(String decisionText);
    protected abstract double getValueIfResolvedOn(String player, DefaultLotroGame game, PhysicalCard target);
    protected abstract ArrayList<PhysicalCard> getPotentialTargets(String player, DefaultLotroGame game);
    protected abstract boolean affectsAll();
    public abstract String toString(String player, DefaultLotroGame game, List<PhysicalCard> targets);

    private double getValueIfResolvedWithTarget(String player, DefaultLotroGame game, PhysicalCard target) {
        return getValueIfResolvedWithTargets(player, game, List.of(target));
    }

    private double getValueIfResolvedWithTargets(String player, DefaultLotroGame game, List<PhysicalCard> targets) {
        if (targets == null || targets.isEmpty()) {
            return 0;
        } else {
            return targets.stream().mapToDouble(target -> getValueIfResolvedOn(player, game, target)).sum();
        }
    }

    private PhysicalCard chooseTarget(String player, DefaultLotroGame game) {
        List<PhysicalCard> potentialTargets = getPotentialTargets(player, game);
        if (potentialTargets.isEmpty()) {
            return null;
        } else {
            potentialTargets.sort((o1, o2) -> Double.compare(getValueIfResolvedWithTarget(player, game, o2), getValueIfResolvedWithTarget(player, game, o1)));
            return potentialTargets.getFirst();
        }
    }

    public final List<PhysicalCard> chooseTargets(String player, DefaultLotroGame game, List<List<PhysicalCard>> targets) {
        if (targets == null) {
            return null;
        }
        List<List<PhysicalCard>> potentialCombinations = new ArrayList<>(targets);
        return potentialCombinations.stream().min((o1, o2) -> Double.compare(
                getValueIfResolvedWithTargets(player, game, o2),
                getValueIfResolvedWithTargets(player, game, o1))).orElse(null);
    }

    public final double getMaximumPossibleValue(String player, DefaultLotroGame game) {
        if (affectsAll()) {
            return getValueIfResolvedWithTargets(player, game, getPotentialTargets(player, game));
        } else {
            PhysicalCard bestTarget = chooseTarget(player, game);
            if (bestTarget == null) {
                return 0;
            } else {
                return getValueIfResolvedWithTarget(player, game, bestTarget);
            }
        }
    }

    @Override
    public final double getValueIfResolved(String player, DefaultLotroGame game) {
        throw new IllegalStateException("Cannot get value without target provided");
    }
    @Override
    public final String toString(String player, DefaultLotroGame game) {
        throw new IllegalStateException("Cannot produce string without targets provided");
    }
}
