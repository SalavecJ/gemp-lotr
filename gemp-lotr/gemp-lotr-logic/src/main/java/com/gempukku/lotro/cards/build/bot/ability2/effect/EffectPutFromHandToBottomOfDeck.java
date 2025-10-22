package com.gempukku.lotro.cards.build.bot.ability2.effect;

import com.gempukku.lotro.cards.build.bot.ability2.util.HandValueUtil;
import com.gempukku.lotro.cards.build.bot.abstractcard.BotCard;
import com.gempukku.lotro.game.state.PlannedBoardState;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class EffectPutFromHandToBottomOfDeck extends EffectWithTarget {
    protected final Predicate<BotCard> targetPredicate;

    public EffectPutFromHandToBottomOfDeck(Predicate<BotCard> targetPredicate) {
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
        List<BotCard> potentialTargets = getPotentialTargets(player, plannedBoardState);
        if (potentialTargets.isEmpty()) {
            return null;
        } else {
            potentialTargets.sort((o1, o2) -> Double.compare(getValueOfTarget(player, o2, plannedBoardState), getValueOfTarget(player, o1, plannedBoardState)));
            return potentialTargets.getFirst();
        }
    }

    @Override
    public String toString(String player, PlannedBoardState plannedBoardState, List<BotCard> targets) {
        if (targets.isEmpty()) {
            return "attempt to put cards from hand to the bottom of deck, but none can be chosen";
        } else {
            String joined = targets.stream()
                    .map(t -> t.getSelf().getBlueprint().getFullName())
                    .collect(Collectors.joining("; "));
            return  "put card(s) from hand to the bottom of deck: " + joined;
        }
    }

    @Override
    public void resolve(String player, PlannedBoardState plannedBoardState) {
        BotCard target = chooseTarget(player, plannedBoardState);
        if (target == null) return;
        resolveWithTarget(player, plannedBoardState, target);
    }

    @Override
    public void resolveWithTarget(String player, PlannedBoardState plannedBoardState, BotCard target) {
        if (target == null) {
            return;
        } else {
            plannedBoardState.moveFromHandToBottomOfDeck(target);
        }
    }

    @Override
    public double getValueIfResolved(String player, PlannedBoardState plannedBoardState) {
        BotCard target = chooseTarget(player, plannedBoardState);
        return getValueIfResolvedWithTarget(player, plannedBoardState, target);
    }

    @Override
    public double getValueIfResolvedWithTarget(String player, PlannedBoardState plannedBoardState, BotCard target) {
        return getValueOfTarget(player, target, plannedBoardState);
    }

    public double getValueOfTarget(String player, BotCard target, PlannedBoardState plannedBoardState) {
        if (target == null) {
            return 0;
        }
        double targetValue = HandValueUtil.cardValueInHand(target, plannedBoardState);
        if (player.equals(target.getSelf().getOwner())){
            if (targetValue > 0) {
                return -targetValue;
            } else {
                return 0.5;
            }
        } else {
            if (targetValue > 0) {
                return targetValue;
            } else {
                return -0.5;
            }
        }
    }
}
