package com.gempukku.lotro.cards.build.bot.ability2.cost;

import com.gempukku.lotro.cards.build.bot.abstractcard.BotCard;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.game.state.PlannedBoardState;

public class CostExertSelf extends Cost {
    protected final int amount;

    public CostExertSelf(int amount) {
        this.amount = amount;
    }

    @Override
    public void pay(BotCard source, PlannedBoardState plannedBoardState) {
        if (!canPayCost(source, plannedBoardState)) {
            throw new IllegalStateException("Cost cannot be payed");
        }

        plannedBoardState.exert(source, amount);
    }

    @Override
    public boolean canPayCost(BotCard source, PlannedBoardState plannedBoardState) {
        return plannedBoardState.getVitality(source) > amount;
    }

    @Override
    public double getValueIfPayed(BotCard source, PlannedBoardState plannedBoardState) {
        if (!canPayCost(source, plannedBoardState)) {
            throw new IllegalStateException("Cost cannot be payed");
        }

        double amount = Math.min(this.amount, plannedBoardState.getVitality(source) - 1);

        double value = amount;
        // exerting an ally has lower impact
        if (source.getSelf().getBlueprint().getCardType().equals(CardType.ALLY)) {
            value /= 2.0;
        }
        // exhausting a companion has higher impact
        if (source.getSelf().getBlueprint().getCardType().equals(CardType.COMPANION)
                && plannedBoardState.getVitality(source) - amount == 1) {
            value += 0.5;
        }
        // exerting my own cards is negative value, opposite for opponent's cards
        return source.getSelf().getOwner().equals(source.getSelf().getOwner()) ? -value : value;
    }
}
