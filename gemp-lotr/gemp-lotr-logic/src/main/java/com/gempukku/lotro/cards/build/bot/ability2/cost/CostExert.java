package com.gempukku.lotro.cards.build.bot.ability2.cost;

import com.gempukku.lotro.cards.build.bot.BotTargetingMode;
import com.gempukku.lotro.cards.build.bot.abstractcard.BotCard;
import com.gempukku.lotro.common.CardType;
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
        double amount = Math.min(this.amount, plannedBoardState.getVitality(toBeExerted) - 1);

        double value = amount;
        // exerting an ally has lower impact
        if (toBeExerted.getSelf().getBlueprint().getCardType().equals(CardType.ALLY)) {
            value /= 2.0;
        }
        // exhausting a companion has higher impact
        if (toBeExerted.getSelf().getBlueprint().getCardType().equals(CardType.COMPANION)
                && plannedBoardState.getVitality(toBeExerted) - amount == 1) {
            value += 0.5;
        }
        // exerting my own cards is negative value, opposite for opponent's cards
        return toBeExerted.getSelf().getOwner().equals(player) ? -value : value;
    }
}
