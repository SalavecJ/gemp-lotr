package com.gempukku.lotro.cards.build.bot.ability2.cost;

import com.gempukku.lotro.cards.build.bot.abstractcard.BotCard;
import com.gempukku.lotro.game.state.PlannedBoardState;

public class CostDiscardSelf extends Cost {

    public CostDiscardSelf() {

    }

    @Override
    public void pay(BotCard source, PlannedBoardState plannedBoardState) {
        if (!canPayCost(source, plannedBoardState)) {
            throw new IllegalStateException("Cost cannot be payed");
        }

        plannedBoardState.discardFromPlay(source);
    }

    @Override
    public boolean canPayCost(BotCard source, PlannedBoardState plannedBoardState) {
        return plannedBoardState.getActiveCards().contains(source);
    }

    @Override
    public double getValueIfPayed(BotCard source, PlannedBoardState plannedBoardState) {
        if (!canPayCost(source, plannedBoardState)) {
            throw new IllegalStateException("Cost cannot be payed");
        }

        return -1.1;
    }
}
