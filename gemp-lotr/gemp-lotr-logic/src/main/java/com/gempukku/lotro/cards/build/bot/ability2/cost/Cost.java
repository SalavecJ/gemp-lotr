package com.gempukku.lotro.cards.build.bot.ability2.cost;

import com.gempukku.lotro.cards.build.bot.ability2.Target;
import com.gempukku.lotro.cards.build.bot.abstractcard.BotCard;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.game.state.PlannedBoardState;

import java.util.function.Predicate;

public abstract class Cost {
    public abstract void pay(String player, PlannedBoardState plannedBoardState);
    public abstract boolean canPayCost(String player, PlannedBoardState plannedBoardState);
    public abstract double getValueIfPayed(String player, PlannedBoardState plannedBoardState);


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

    public static CostExert exert(Race race) {
        return exert(race, 1);
    }

    public static CostExert exert(Race race, int amount) {
        return exert(Target.race(race), amount);
    }

    public static CostExertSelf exertSelf(BotCard self) {
        return exertSelf(self, 1);
    }

    public static CostExertSelf exertSelf(BotCard self, int amount) {
        return new CostExertSelf(self, amount);
    }

    public static CostDiscardSelf discardSelf(BotCard self) {
        return new CostDiscardSelf(self);
    }

    public static CostDiscardCardFromHand discardFromHand(Predicate<BotCard> target) {
        return new CostDiscardCardFromHand(target);
    }

    public static CostAddTwilight addTwilight(int amount) {
        return new CostAddTwilight(amount);
    }
}
