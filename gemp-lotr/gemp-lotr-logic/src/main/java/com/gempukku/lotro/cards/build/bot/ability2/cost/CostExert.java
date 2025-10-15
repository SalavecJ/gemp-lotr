package com.gempukku.lotro.cards.build.bot.ability2.cost;

import com.gempukku.lotro.cards.build.bot.abstractcard.BotCard;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.game.state.PlannedBoardState;

import java.util.List;
import java.util.function.Predicate;

public class CostExert extends Cost {
    protected final Predicate<BotCard> targetPredicate;
    protected final int amount;

    public CostExert(Predicate<BotCard> targetPredicate, int amount) {
        this.targetPredicate = targetPredicate;
        this.amount = amount;
    }

    private List<BotCard> getPotentialTargets(PlannedBoardState plannedBoardState) {
        return plannedBoardState.getActiveCards().stream().filter(targetPredicate).toList();
    }

    @Override
    public void pay(BotCard source, PlannedBoardState plannedBoardState) {
        if (!canPayCost(source, plannedBoardState)) {
            throw new IllegalStateException("Cost cannot be payed");
        }

        List<BotCard> potentialTargets = getPotentialTargets(plannedBoardState);

        if (potentialTargets.size() != 1) {
            throw new IllegalStateException("Cannot resolve exerting cost if number of potential targets is not 1");
        }

        plannedBoardState.exert(potentialTargets.getFirst(), amount);
    }

    @Override
    public boolean canPayCost(BotCard source, PlannedBoardState plannedBoardState) {
        List<BotCard> potentialTargets = getPotentialTargets(plannedBoardState);
        return potentialTargets.stream().anyMatch(botCard -> plannedBoardState.getVitality(botCard) > amount);
    }

    @Override
    public double getValueIfPayed(BotCard source, PlannedBoardState plannedBoardState) {
        if (!canPayCost(source, plannedBoardState)) {
            throw new IllegalStateException("Cost cannot be payed");
        }

        List<BotCard> potentialTargets = getPotentialTargets(plannedBoardState);

        if (potentialTargets.size() != 1) {
            throw new IllegalStateException("Cannot resolve exerting cost if number of potential targets is not 1");
        }

        BotCard toBeExerted = potentialTargets.getFirst();
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
        return toBeExerted.getSelf().getOwner().equals(source.getSelf().getOwner()) ? -value : value;
    }
}
