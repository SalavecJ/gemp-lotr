package com.gempukku.lotro.bots.forge.cards.ability2.effect;

import com.gempukku.lotro.bots.forge.cards.ability2.util.WoundsValueUtil;
import com.gempukku.lotro.bots.forge.cards.abstractcard.BotCard;
import com.gempukku.lotro.bots.forge.plan.PlannedBoardState;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class EffectHeal extends EffectWithTarget {
    protected final Predicate<BotCard> targetPredicate;
    protected final int amount;

    public EffectHeal(Predicate<BotCard> targetPredicate, int amount) {
        this.targetPredicate = targetPredicate;
        this.amount = amount;
    }

    @Override
    public ArrayList<BotCard> getPotentialTargets(String player, PlannedBoardState plannedBoardState) {
        return new ArrayList<>(plannedBoardState.getActiveCards().stream()
                .filter(targetPredicate)
                .filter(botCard -> plannedBoardState.getWounds(botCard) > 0)
                .toList());
    }

    @Override
    public boolean affectsAll() {
        return false;
    }

    @Override
    public String toString(String player, PlannedBoardState plannedBoardState, List<BotCard> targets) {
        if (targets.isEmpty()) {
            return "attempt to heal a character, but none can be chosen";
        } else {
            String joined = targets.stream()
                    .map(t -> t.getSelf().getBlueprint().getFullName())
                    .collect(Collectors.joining("; "));
            if (amount == 1) {
                return "remove a wound from " + joined;
            } else {
                return "remove " + amount + "wounds from " + joined;
            }
        }
    }

    @Override
    protected void resolveOn(String player, PlannedBoardState plannedBoardState, BotCard target) {
        plannedBoardState.heal(target, amount);
    }

    @Override
    protected double getValueIfResolvedOn(String player, PlannedBoardState plannedBoardState, BotCard target) {
        return WoundsValueUtil.evaluateWoundsChangeValue(player, plannedBoardState, target, -amount);
    }
}
