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
    public final ArrayList<BotCard> getPotentialTargets(String player, PlannedBoardState plannedBoardState) {
        return new ArrayList<>(plannedBoardState.getHand(player).stream().filter(targetPredicate).filter(botCard -> botCard.canBePlayed(plannedBoardState)).toList());
    }

    @Override
    public final boolean affectsAll() {
        return false;
    }
}
