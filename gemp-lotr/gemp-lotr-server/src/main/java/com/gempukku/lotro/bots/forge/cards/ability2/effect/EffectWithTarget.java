package com.gempukku.lotro.bots.forge.cards.ability2.effect;

import com.gempukku.lotro.bots.forge.cards.abstractcard.BotCard;
import com.gempukku.lotro.bots.forge.plan.PlannedBoardState;

import java.util.ArrayList;
import java.util.List;

public abstract class EffectWithTarget extends Effect{
    public final void resolveWithTarget(String player, PlannedBoardState plannedBoardState, BotCard target) {
        resolveWithTargets(player, plannedBoardState, List.of(target));
    }
    public final void resolveWithTargets(String player, PlannedBoardState plannedBoardState, List<BotCard> targets) {
        if (targets == null || targets.isEmpty()) {
            return;
        } else {
            for (BotCard target : targets) {
                resolveOn(player, plannedBoardState, target);
            }
        }
    }
    protected abstract void resolveOn(String player, PlannedBoardState plannedBoardState, BotCard target);

    public final double getValueIfResolvedWithTarget(String player, PlannedBoardState plannedBoardState, BotCard target) {
        return getValueIfResolvedWithTargets(player, plannedBoardState, List.of(target));
    }
    public final double getValueIfResolvedWithTargets(String player, PlannedBoardState plannedBoardState, List<BotCard> targets) {
        if (targets == null || targets.isEmpty()) {
            return 0;
        } else {
            return targets.stream().mapToDouble(target -> getValueIfResolvedOn(player, plannedBoardState, target)).sum();
        }
    }
    protected abstract double getValueIfResolvedOn(String player, PlannedBoardState plannedBoardState, BotCard target);

    public abstract String toString(String player, PlannedBoardState plannedBoardState, List<BotCard> targets);

    public abstract ArrayList<BotCard> getPotentialTargets(String player, PlannedBoardState plannedBoardState);
    public abstract boolean affectsAll();

    public final BotCard chooseTarget(String player, PlannedBoardState plannedBoardState) {
        List<BotCard> potentialTargets = getPotentialTargets(player, plannedBoardState);
        if (potentialTargets.isEmpty()) {
            return null;
        } else {
            potentialTargets.sort((o1, o2) -> Double.compare(getValueIfResolvedWithTarget(player, plannedBoardState, o2), getValueIfResolvedWithTarget(player, plannedBoardState, o1)));
            return potentialTargets.getFirst();
        }
    }

    public final double getMaximumPossibleValue(String player, PlannedBoardState plannedBoardState) {
        if (affectsAll()) {
            return getValueIfResolvedWithTargets(player, plannedBoardState, getPotentialTargets(player, plannedBoardState));
        } else {
            BotCard bestTarget = chooseTarget(player, plannedBoardState);
            if (bestTarget == null) {
                return 0;
            } else {
                return getValueIfResolvedWithTarget(player, plannedBoardState, bestTarget);
            }
        }
    }

    @Override
    public final void resolve(String player, PlannedBoardState plannedBoardState) {
        throw new IllegalStateException("Cannot pay cost without target provided");
    }
    @Override
    public final double getValueIfResolved(String player, PlannedBoardState plannedBoardState) {
        throw new IllegalStateException("Cannot get value without target provided");
    }
    @Override
    public final String toString(String player, PlannedBoardState plannedBoardState) {
        throw new IllegalStateException("Cannot produce string without targets provided");
    }
}
