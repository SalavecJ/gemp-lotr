package com.gempukku.lotro.bots.forge.cards.ability2.effect;

import com.gempukku.lotro.bots.forge.cards.ability2.util.HandValueUtil;
import com.gempukku.lotro.bots.forge.cards.abstractcard.BotCard;
import com.gempukku.lotro.bots.forge.plan.PlannedBoardState;

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
    protected void resolveOn(String player, PlannedBoardState plannedBoardState, BotCard target) {
        plannedBoardState.moveFromHandToBottomOfDeck(target);
    }

    @Override
    protected double getValueIfResolvedOn(String player, PlannedBoardState plannedBoardState, BotCard target) {
        if (target == null) {
            return 0;
        }
        double targetValue = HandValueUtil.cardValueInHand(target, plannedBoardState);
        if (player.equals(target.getSelf().getOwner())){
            if (targetValue > 0) {
                return -targetValue;
            } else {
                return 1;
            }
        } else {
            if (targetValue > 0) {
                return targetValue;
            } else {
                return -1;
            }
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
}
