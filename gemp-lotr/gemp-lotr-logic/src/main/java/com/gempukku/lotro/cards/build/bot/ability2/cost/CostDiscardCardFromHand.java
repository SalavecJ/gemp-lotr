package com.gempukku.lotro.cards.build.bot.ability2.cost;

import com.gempukku.lotro.cards.build.bot.ability2.util.HandValueUtil;
import com.gempukku.lotro.cards.build.bot.abstractcard.BotCard;
import com.gempukku.lotro.game.state.PlannedBoardState;

import java.util.ArrayList;
import java.util.List;
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
    public String toString(String player, PlannedBoardState plannedBoardState, BotCard target) {
        return "discard card from hand: " + target.getSelf().getBlueprint().getFullName();
    }

    @Override
    public void pay(String player, PlannedBoardState plannedBoardState) {
        if (!canPayCost(player, plannedBoardState)) {
            throw new IllegalStateException("Cost cannot be payed");
        }
        BotCard target = chooseTarget(player, plannedBoardState);
        if (target == null) return;
        plannedBoardState.discardFromHand(target);
    }

    @Override
    public boolean canPayCost(String player, PlannedBoardState plannedBoardState) {
        return !getPotentialTargets(player, plannedBoardState).isEmpty();
    }

    @Override
    public double getValueIfPayed(String player, PlannedBoardState plannedBoardState) {
        BotCard target = chooseTarget(player, plannedBoardState);
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
