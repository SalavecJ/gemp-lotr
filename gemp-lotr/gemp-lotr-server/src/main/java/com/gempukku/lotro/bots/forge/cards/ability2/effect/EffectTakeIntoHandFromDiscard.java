package com.gempukku.lotro.bots.forge.cards.ability2.effect;

import com.gempukku.lotro.bots.forge.cards.ability2.util.HandValueUtil;
import com.gempukku.lotro.bots.forge.cards.abstractcard.BotCard;
import com.gempukku.lotro.bots.forge.plan.PlannedBoardState;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class EffectTakeIntoHandFromDiscard extends EffectWithTarget{
    protected final Predicate<BotCard> targetPredicate;

    public EffectTakeIntoHandFromDiscard(Predicate<BotCard> targetPredicate) {
        this.targetPredicate = targetPredicate;
    }

    @Override
    public ArrayList<BotCard> getPotentialTargets(String player, PlannedBoardState plannedBoardState) {
        return new ArrayList<>(plannedBoardState.getDiscard(player).stream().filter(targetPredicate).toList());
    }

    @Override
    public boolean affectsAll() {
        return false;
    }

    @Override
    protected void resolveOn(String player, PlannedBoardState plannedBoardState, BotCard target) {
        plannedBoardState.moveFromDiscardIntoHand(target);
    }

    @Override
    protected double getValueIfResolvedOn(String player, PlannedBoardState plannedBoardState, BotCard target) {
        if (target == null) {
            return 0;
        }
        return HandValueUtil.cardValueInHand(target, plannedBoardState);
    }

    @Override
    public String toString(String player, PlannedBoardState plannedBoardState, List<BotCard> targets) {
        if (targets.isEmpty()) {
            return "attempt to take card from discard, but none can be chosen";
        } else {
            String joined = targets.stream()
                    .map(t -> t.getSelf().getBlueprint().getFullName())
                    .collect(Collectors.joining("; "));
            return  "take into hand card(s) from discard: " + joined;
        }
    }
}
