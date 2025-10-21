package com.gempukku.lotro.cards.build.bot.ability2;

import com.gempukku.lotro.cards.build.bot.ability2.cost.Cost;
import com.gempukku.lotro.cards.build.bot.ability2.effect.Effect;
import com.gempukku.lotro.game.state.PlannedBoardState;

public class EventAbility implements Ability {
    protected final Effect effect;
    protected final Cost cost;

    public EventAbility(Effect effect, Cost cost) {
        this.effect = effect;
        this.cost = cost;
    }

    public Effect getEffect() {
        return effect;
    }

    public Cost getCost() {
        return cost;
    }

    @Override
    public void resolveAbility(String player, PlannedBoardState plannedBoardState) {
        if (cost != null)
            cost.pay(player, plannedBoardState);
        effect.resolve(player, plannedBoardState);
    }

    @Override
    public boolean canPayCost(String player, PlannedBoardState plannedBoardState) {
        if (cost == null) return true;
        return cost.canPayCost(player, plannedBoardState);
    }

    @Override
    public double getValueIfUsed(String player, PlannedBoardState plannedBoardState) {
        double costValue = cost != null ? cost.getValueIfPayed(player, plannedBoardState) : 0.0;
        return effect.getValueIfResolved(player, plannedBoardState) + costValue;
    }

    @Override
    public boolean conditionOk(String player, PlannedBoardState plannedBoardState) {
        return true;
    }
}
