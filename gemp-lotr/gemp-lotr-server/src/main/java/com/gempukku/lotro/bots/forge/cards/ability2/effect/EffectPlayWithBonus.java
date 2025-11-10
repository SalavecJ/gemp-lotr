package com.gempukku.lotro.bots.forge.cards.ability2.effect;

import com.gempukku.lotro.bots.forge.cards.abstractcard.BotCard;
import com.gempukku.lotro.bots.forge.plan.PlannedBoardState;

import java.util.ArrayList;
import java.util.function.Predicate;

public abstract class EffectPlayWithBonus extends EffectWithTarget {
    protected final Predicate<BotCard> targetPredicate;

    public EffectPlayWithBonus(Predicate<BotCard> targetPredicate) {
        this.targetPredicate = targetPredicate;
    }

    @Override
    public ArrayList<BotCard> getPotentialTargets(String player, PlannedBoardState plannedBoardState) {
        return new ArrayList<>(plannedBoardState.getHand(player).stream().filter(targetPredicate).filter(botCard -> botCard.canBePlayed(plannedBoardState)).toList());
    }

    @Override
    public boolean affectsAll() {
        return false;
    }

    @Override
    public BotCard chooseTarget(String player, PlannedBoardState plannedBoardState) {
        if (getPotentialTargets(player, plannedBoardState).isEmpty()) {
            throw new IllegalStateException("No valid targets for EffectPlayWithBonus");
        }
        return getPotentialTargets(player, plannedBoardState).getFirst();
    }

    @Override
    public void resolve(String player, PlannedBoardState plannedBoardState) {
        throw new IllegalStateException("Cannot choose target automatically for EffectPlayWithBonus");
    }
}
