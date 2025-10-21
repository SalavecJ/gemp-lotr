package com.gempukku.lotro.cards.build.bot.ability2.effect;

import com.gempukku.lotro.cards.build.bot.ability2.util.HandValueUtil;
import com.gempukku.lotro.cards.build.bot.abstractcard.BotCard;
import com.gempukku.lotro.game.state.PlannedBoardState;

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
    public BotCard chooseTarget(String player, PlannedBoardState plannedBoardState) {
        List<BotCard> potentialTargets = getPotentialTargets(player, plannedBoardState);
        if (potentialTargets.isEmpty()) {
            return null;
        } else {
            potentialTargets.sort((o1, o2) -> Double.compare(getValueOfTarget(o2, plannedBoardState), getValueOfTarget(o1, plannedBoardState)));
            return potentialTargets.getFirst();
        }
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

    @Override
    public void resolve(String player, PlannedBoardState plannedBoardState) {
        BotCard target = chooseTarget(player, plannedBoardState);
        if (target == null) return;
        plannedBoardState.moveFromDiscardIntoHand(target);
    }

    @Override
    public double getValueIfResolved(String player, PlannedBoardState plannedBoardState) {
        BotCard target = chooseTarget(player, plannedBoardState);
        return getValueOfTarget(target, plannedBoardState);
    }

    public double getValueOfTarget(BotCard target, PlannedBoardState plannedBoardState) {
        if (target == null) {
            return 0;
        }
        return HandValueUtil.cardValueInHand(target, plannedBoardState);
    }
}
