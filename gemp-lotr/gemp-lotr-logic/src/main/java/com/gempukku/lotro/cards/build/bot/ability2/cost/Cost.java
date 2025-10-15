package com.gempukku.lotro.cards.build.bot.ability2.cost;

import com.gempukku.lotro.cards.build.bot.ability2.Target;
import com.gempukku.lotro.cards.build.bot.abstractcard.BotCard;
import com.gempukku.lotro.game.state.PlannedBoardState;

import java.util.function.Predicate;

public abstract class Cost {
    public abstract void pay(BotCard source, PlannedBoardState plannedBoardState);
    public abstract boolean canPayCost(BotCard source, PlannedBoardState plannedBoardState);
    public abstract double getValueIfPayed(BotCard source, PlannedBoardState plannedBoardState);


    public static CostExert exert(Predicate<BotCard> target) {
        return exert(target, 1);
    }

    public static CostExert exert(Predicate<BotCard> target, int amount) {
        return new CostExert(target, amount);
    }

    public static CostExert exert(String title) {
        return exert(title, 1);
    }

    public static CostExert exert(String title, int amount) {
        return exert(Target.title(title), amount);
    }

    public static CostExertSelf exertSelf() {
        return exertSelf(1);
    }

    public static CostExertSelf exertSelf(int amount) {
        return new CostExertSelf(amount);
    }

    public static CostDiscardSelf discardSelf() {
        return new CostDiscardSelf();
    }
}
