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
    protected void resolveOn(String player, PlannedBoardState plannedBoardState, BotCard target) {
        plannedBoardState.discardFromPlay(target);
    }

    @Override
    protected double getValueIfResolvedOn(String player, PlannedBoardState plannedBoardState, BotCard target) {
        return target.getSelf().getOwner().equals(player) ? -1.1: 1.1;
    }
}
