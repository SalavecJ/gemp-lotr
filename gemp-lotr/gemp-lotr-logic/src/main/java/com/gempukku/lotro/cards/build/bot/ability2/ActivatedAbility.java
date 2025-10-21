package com.gempukku.lotro.cards.build.bot.ability2;

import com.gempukku.lotro.cards.build.bot.ability2.condition.Condition;
import com.gempukku.lotro.cards.build.bot.ability2.cost.Cost;
import com.gempukku.lotro.cards.build.bot.ability2.effect.Effect;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.state.PlannedBoardState;

public class ActivatedAbility implements Ability {
    protected final Phase phase;

    protected final Condition condition;
    protected final Effect effect;
    protected final Cost cost;

    public ActivatedAbility(Phase phase, Condition condition, Effect effect, Cost cost) {
        this.phase = phase;
        this.condition = condition;
        this.effect = effect;
        this.cost = cost;
    }

    public Phase getPhase() {
        return phase;
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
    public boolean conditionOk(String player, PlannedBoardState plannedBoardState) {
        if (condition == null) return true;
        return condition.isOk(player, plannedBoardState);
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
}
