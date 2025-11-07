package com.gempukku.lotro.cards.build.bot.ability2.cost;

import com.gempukku.lotro.cards.build.bot.abstractcard.BotCard;
import com.gempukku.lotro.game.state.PlannedBoardState;

import java.util.ArrayList;

public abstract class CostWithTarget extends Cost {
    public abstract void payWithTarget(String player, PlannedBoardState plannedBoardState, BotCard target);
    public abstract boolean canPayCostWithTarget(String player, PlannedBoardState plannedBoardState, BotCard target);
    public abstract double getValueIfPayedWithTarget(String player, PlannedBoardState plannedBoardState, BotCard target);

    public abstract ArrayList<BotCard> getPotentialTargets(String player, PlannedBoardState plannedBoardState);
    public abstract BotCard chooseTarget(String player, PlannedBoardState plannedBoardState);
    public abstract String toString(String player, PlannedBoardState plannedBoardState, BotCard target);
    public String toString(String player, PlannedBoardState plannedBoardState) {
        throw new IllegalStateException("Cannot produce string without targets provided");
    }
}
