package com.gempukku.lotro.bots.forge.cards.ability2.cost;

import com.gempukku.lotro.bots.forge.cards.ability2.Target;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.function.Predicate;

public abstract class Cost {
    public abstract boolean canPayCost(String player, DefaultLotroGame game);
    public abstract double getValueIfPayed(String player, DefaultLotroGame game);
    public abstract String toString(String player, DefaultLotroGame game);


    public static CostExert exert(Predicate<PhysicalCard> target) {
        return exert(target, 1);
    }

    public static CostExert exert(Predicate<PhysicalCard> target, int amount) {
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

    public static CostExertSelf exertSelf(PhysicalCard self) {
        return exertSelf(self, 1);
    }

    public static CostExertSelf exertSelf(PhysicalCard self, int amount) {
        return new CostExertSelf(self, amount);
    }

    public static CostDiscardSelf discardSelf(PhysicalCard self) {
        return new CostDiscardSelf(self);
    }

    public static CostDiscardCardFromHand discardFromHand(Predicate<PhysicalCard> target) {
        return discardFromHand(target, 1);
    }

    public static CostDiscardCardFromHand discardFromHand(int howMany) {
        return new CostDiscardCardFromHand(botCard -> true, howMany);
    }

    public static CostDiscardCardFromHand discardFromHand(Predicate<PhysicalCard> target, int howMany) {
        return new CostDiscardCardFromHand(target, howMany);
    }

    public static CostAddTwilight addTwilight(int amount) {
        return new CostAddTwilight(amount);
    }

    public static CostRemoveTwilight removeTwilight(int amount) {
        return new CostRemoveTwilight(amount);
    }
}
