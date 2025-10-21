package com.gempukku.lotro.cards.build.bot.ability2.cost;

import com.gempukku.lotro.cards.build.bot.abstractcard.BotCard;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.game.state.PlannedBoardState;

public class CostExertSelf extends Cost {
    private final BotCard self;
    protected final int amount;

    public CostExertSelf(BotCard self, int amount) {
        this.self = self;
        this.amount = amount;
    }

    @Override
    public void pay(String player, PlannedBoardState plannedBoardState) {
        if (!canPayCost(player, plannedBoardState)) {
            throw new IllegalStateException("Cost cannot be payed");
        }

        plannedBoardState.exert(self, amount);
    }

    @Override
    public boolean canPayCost(String player, PlannedBoardState plannedBoardState) {
        return plannedBoardState.getVitality(self) > amount;
    }

    @Override
    public double getValueIfPayed(String player, PlannedBoardState plannedBoardState) {
        if (!canPayCost(player, plannedBoardState)) {
            throw new IllegalStateException("Cost cannot be payed");
        }

        double amount = Math.min(this.amount, plannedBoardState.getVitality(self) - 1);

        double value = amount;
        // exerting an ally has lower impact
        if (self.getSelf().getBlueprint().getCardType().equals(CardType.ALLY)) {
            value /= 2.0;
        }
        // exhausting a companion has higher impact
        if (self.getSelf().getBlueprint().getCardType().equals(CardType.COMPANION)
                && plannedBoardState.getVitality(self) - amount == 1) {
            value += 0.5;
        }
        // exerting my own cards is negative value, opposite for opponent's cards
        return self.getSelf().getOwner().equals(player) ? -value : value;
    }

    @Override
    public String toString(String player, PlannedBoardState plannedBoardState) {
        if (amount == 1) {
            return "exert self (" + self.getSelf().getBlueprint().getFullName() + ")";
        } else {
            return "exert self " + amount + " times (" + self.getSelf().getBlueprint().getFullName() + ")";
        }
    }
}
