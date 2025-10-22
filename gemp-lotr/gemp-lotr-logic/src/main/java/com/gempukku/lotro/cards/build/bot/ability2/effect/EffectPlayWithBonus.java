package com.gempukku.lotro.cards.build.bot.ability2.effect;

import com.gempukku.lotro.cards.build.bot.abstractcard.BotCard;
import com.gempukku.lotro.game.state.PlannedBoardState;

import java.util.ArrayList;
import java.util.function.Predicate;

public abstract class EffectPlayWithBonus extends EffectWithTarget {
    protected final Predicate<BotCard> targetPredicate;

    public EffectPlayWithBonus(Predicate<BotCard> targetPredicate) {
        this.targetPredicate = targetPredicate;
    }

    @Override
    public ArrayList<BotCard> getPotentialTargets(String player, PlannedBoardState plannedBoardState) {
        return new ArrayList<>(plannedBoardState.getHand(player).stream().filter(targetPredicate).toList());
    }

    @Override
    public boolean affectsAll() {
        return false;
    }

    @Override
    public BotCard chooseTarget(String player, PlannedBoardState plannedBoardState) {
        throw new IllegalStateException("Cannot choose target automatically for EffectPlayWithBonus");
    }

    @Override
    public void resolve(String player, PlannedBoardState plannedBoardState) {
        throw new IllegalStateException("Cannot choose target automatically for EffectPlayWithBonus");
    }

    @Override
    public double getValueIfResolved(String player, PlannedBoardState plannedBoardState) {
        throw new IllegalStateException("Cannot choose target automatically for EffectPlayWithBonus");
    }
}
