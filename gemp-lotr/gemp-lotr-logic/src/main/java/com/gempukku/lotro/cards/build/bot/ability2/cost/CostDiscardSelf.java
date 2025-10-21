package com.gempukku.lotro.cards.build.bot.ability2.cost;

import com.gempukku.lotro.cards.build.bot.abstractcard.BotCard;
import com.gempukku.lotro.game.state.PlannedBoardState;

public class CostDiscardSelf extends Cost {
    private final BotCard self;

    public CostDiscardSelf(BotCard self) {
        this.self = self;
    }

    @Override
    public void pay(String player, PlannedBoardState plannedBoardState) {
        if (!canPayCost(player, plannedBoardState)) {
            throw new IllegalStateException("Cost cannot be payed");
        }

        plannedBoardState.discardFromPlay(self);
    }

    @Override
    public boolean canPayCost(String player, PlannedBoardState plannedBoardState) {
        return plannedBoardState.getActiveCards().contains(self);
    }

    @Override
    public double getValueIfPayed(String player, PlannedBoardState plannedBoardState) {
        if (!canPayCost(player, plannedBoardState)) {
            throw new IllegalStateException("Cost cannot be payed");
        }

        return -1.1;
    }

    @Override
    public String toString(String player, PlannedBoardState plannedBoardState) {
        return "discard self (" + self.getSelf().getBlueprint().getFullName() + ")";
    }
}
