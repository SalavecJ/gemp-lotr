package com.gempukku.lotro.cards.build.bot.ability2.cost;

import com.gempukku.lotro.cards.build.bot.abstractcard.BotCard;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.state.PlannedBoardState;

public class CostAddTwilight extends Cost {
    protected final int amount;

    public CostAddTwilight(int amount) {
        this.amount = amount;
    }

    @Override
    public void pay(BotCard source, PlannedBoardState plannedBoardState) {
        plannedBoardState.addTwilight(amount);
    }

    @Override
    public boolean canPayCost(BotCard source, PlannedBoardState plannedBoardState) {
        return true;
    }

    @Override
    public double getValueIfPayed(BotCard source, PlannedBoardState plannedBoardState) {
        double returnValue = (double) amount / 10;
        if (Side.FREE_PEOPLE.equals(source.getSelf().getBlueprint().getSide())) {
            returnValue *= -1;
        }

        return returnValue;
    }
}
