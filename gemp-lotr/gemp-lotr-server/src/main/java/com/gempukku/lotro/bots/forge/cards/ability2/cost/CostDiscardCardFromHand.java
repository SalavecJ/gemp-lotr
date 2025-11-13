package com.gempukku.lotro.bots.forge.cards.ability2.cost;

import com.gempukku.lotro.bots.forge.cards.ability2.util.HandValueUtil;
import com.gempukku.lotro.bots.forge.cards.abstractcard.BotCard;
import com.gempukku.lotro.bots.forge.plan.PlannedBoardState;

import java.util.ArrayList;
import java.util.function.Predicate;

public class CostDiscardCardFromHand extends CostWithTarget {
    protected final Predicate<BotCard> targetPredicate;

    public CostDiscardCardFromHand(Predicate<BotCard> targetPredicate) {
        this.targetPredicate = targetPredicate;
    }

    @Override
    public ArrayList<BotCard> getPotentialTargets(String player, PlannedBoardState plannedBoardState) {
        return new ArrayList<>(plannedBoardState.getHand(player).stream().filter(targetPredicate).toList());
    }

    @Override
    public String toString(String player, PlannedBoardState plannedBoardState, BotCard target) {
        return "discard card from hand: " + target.getSelf().getBlueprint().getFullName();
    }

    @Override
    public void payWithTarget(String player, PlannedBoardState plannedBoardState, BotCard target) {
        if (!canPayCostWithTarget(player, plannedBoardState, target)) {
            throw new IllegalStateException("Cost cannot be payed");
        }
        plannedBoardState.discardFromHand(target);
    }

    @Override
    public double getValueIfPayedWithTarget(String player, PlannedBoardState plannedBoardState, BotCard target) {
        if (!canPayCostWithTarget(player, plannedBoardState, target)) {
            throw new IllegalStateException("Cost cannot be payed");
        }
        return getValueOfTarget(target, plannedBoardState);
    }

    public double getValueOfTarget(BotCard target, PlannedBoardState plannedBoardState) {
        if (target == null) {
            return 0;
        }
        double targetValue = HandValueUtil.cardValueInHand(target, plannedBoardState);
        if (targetValue > 0) {
            return -targetValue;
        } else {
            return 0.5;
        }
    }
}
