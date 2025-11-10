package com.gempukku.lotro.bots.forge.cards.ability2.effect;

import com.gempukku.lotro.bots.forge.cards.abstractcard.BotCard;
import com.gempukku.lotro.bots.forge.plan.PlannedBoardState;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class EffectDiscardFromPlay extends EffectWithTarget{
    protected final Predicate<BotCard> targetPredicate;
    protected final boolean discardAll;

    public EffectDiscardFromPlay(Predicate<BotCard> targetPredicate, boolean discardAll) {
        this.targetPredicate = targetPredicate;
        this.discardAll = discardAll;
    }

    @Override
    public ArrayList<BotCard> getPotentialTargets(String player, PlannedBoardState plannedBoardState) {
        return new ArrayList<>(plannedBoardState.getActiveCards().stream().filter(targetPredicate).toList());
    }

    @Override
    public boolean affectsAll() {
        return discardAll;
    }

    @Override
    public BotCard chooseTarget(String player, PlannedBoardState plannedBoardState) {
        throw new IllegalStateException("Choosing targets for Discard from Play effect not implemented");
    }

    @Override
    public String toString(String player, PlannedBoardState plannedBoardState, List<BotCard> targets) {
        if (targets.isEmpty()) {
            return "discard cards from play, but none will be affected";
        } else {
            String joined = targets.stream()
                    .map(t -> t.getSelf().getBlueprint().getFullName())
                    .collect(Collectors.joining("; "));
           return  "discard from play: " + joined;
        }
    }

    @Override
    public void resolve(String player, PlannedBoardState plannedBoardState) {
        List<BotCard> potentialTargets = getPotentialTargets(player, plannedBoardState);
        if (potentialTargets.isEmpty()) {
            return;
        } else if (potentialTargets.size() == 1) {
            resolveWithTarget(player, plannedBoardState, potentialTargets.getFirst());
        } else {
            if (discardAll) {
                potentialTargets.forEach(plannedBoardState::discardFromPlay);
            } else {
                throw new IllegalStateException("Cannot resolve discard effect if number of potential targets is greater than 1 and not all should be discarded");
            }
        }
    }

    @Override
    public void resolveWithTarget(String player, PlannedBoardState plannedBoardState, BotCard target) {
        if (target == null) {
            return;
        } else {
            plannedBoardState.discardFromPlay(target);
        }
    }

    @Override
    public double getValueIfResolved(String player, PlannedBoardState plannedBoardState) {
        List<BotCard> potentialTargets = getPotentialTargets(player, plannedBoardState);
        if (discardAll) {
            double value = 0.0;
            for (BotCard potentialTarget : potentialTargets) {
                // discarding enemy cards good, discarding own cards bad
                value += potentialTarget.getSelf().getOwner().equals(player) ? -1.1: 1.1;
            }
            return value;
        } else {
            if (potentialTargets.isEmpty()) {
                return 0;
            } else if (potentialTargets.size() == 1) {
                return getValueIfResolvedWithTarget(player, plannedBoardState, potentialTargets.getFirst());
            } else {
                throw new IllegalStateException("Cannot resolve discard value if number of potential targets is greater than 1");
            }
        }
    }

    @Override
    public double getValueIfResolvedWithTarget(String player, PlannedBoardState plannedBoardState, BotCard target) {
        if (target == null) {
            return 0;
        } else {
            return target.getSelf().getOwner().equals(player) ? -1.1: 1.1;
        }
    }
}
