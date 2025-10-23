package com.gempukku.lotro.cards.build.bot.ability2.cost;

import com.gempukku.lotro.cards.build.bot.ability2.util.WoundsValueUtil;
import com.gempukku.lotro.cards.build.bot.abstractcard.BotCard;
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

        int amount = Math.min(this.amount, plannedBoardState.getVitality(self) - 1);
        return WoundsValueUtil.evaluateWoundsChangeValue(player, plannedBoardState, self, amount);
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
