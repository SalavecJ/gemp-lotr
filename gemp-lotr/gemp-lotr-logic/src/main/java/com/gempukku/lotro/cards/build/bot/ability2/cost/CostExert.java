package com.gempukku.lotro.cards.build.bot.ability2.cost;

import com.gempukku.lotro.cards.build.bot.BotTargetingMode;
import com.gempukku.lotro.cards.build.bot.ability2.util.WoundsValueUtil;
import com.gempukku.lotro.cards.build.bot.abstractcard.BotCard;
import com.gempukku.lotro.game.state.PlannedBoardState;

import java.util.ArrayList;
import java.util.function.Predicate;

public class CostExert extends CostWithTarget {
    protected final Predicate<BotCard> targetPredicate;
    protected final int amount;

    public CostExert(Predicate<BotCard> targetPredicate, int amount) {
        this.targetPredicate = targetPredicate;
        this.amount = amount;
    }

    @Override
    public ArrayList<BotCard> getPotentialTargets(String player, PlannedBoardState plannedBoardState) {
        return new ArrayList<>(plannedBoardState.getActiveCards().stream()
                .filter(targetPredicate)
                .filter(botCard -> plannedBoardState.getVitality(botCard) > amount)
                .toList());
    }

    @Override
    public BotCard chooseTarget(String player, PlannedBoardState plannedBoardState) {
        if (getPotentialTargets(player, plannedBoardState).isEmpty()) {
            return null;
        }
        return BotTargetingMode.EXERT_SELF.chooseTarget(plannedBoardState, getPotentialTargets(player, plannedBoardState), false);
    }

    @Override
    public String toString(String player, PlannedBoardState plannedBoardState, BotCard target) {
        if (amount == 1) {
            return "exert " + target.getSelf().getBlueprint().getFullName();
        } else {

            return "exert " + amount + " times " + target.getSelf().getBlueprint().getFullName();
        }
    }

    @Override
    public void pay(String player, PlannedBoardState plannedBoardState) {
        if (!canPayCost(player, plannedBoardState)) {
            throw new IllegalStateException("Cost cannot be payed");
        }

        BotCard target = chooseTarget(player, plannedBoardState);
        if (target == null) return;
        plannedBoardState.exert(target, amount);
    }

    @Override
    public boolean canPayCost(String player, PlannedBoardState plannedBoardState) {
        return !getPotentialTargets(player, plannedBoardState).isEmpty();
    }

    @Override
    public double getValueIfPayed(String player, PlannedBoardState plannedBoardState) {
        if (!canPayCost(player, plannedBoardState)) {
            throw new IllegalStateException("Cost cannot be payed");
        }

        BotCard toBeExerted = chooseTarget(player, plannedBoardState);
        int vitality = plannedBoardState.getVitality(toBeExerted);
        return WoundsValueUtil.evaluateWoundsChangeValue(player, plannedBoardState, toBeExerted, Math.min(amount, vitality - 1));
    }
}
