package com.gempukku.lotro.cards.build.bot.ability2;

import com.gempukku.lotro.cards.build.bot.ability2.cost.Cost;
import com.gempukku.lotro.cards.build.bot.ability2.effect.Effect;
import com.gempukku.lotro.cards.build.bot.abstractcard.BotCard;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.state.PlannedBoardState;

public class ActivatedAbility implements Ability {
    protected final Phase phase;

    protected final Effect effect;
    protected final Cost cost;

    public ActivatedAbility(Phase phase, Effect effect, Cost cost) {
        this.phase = phase;
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
    public void resolveAbility(BotCard source, PlannedBoardState plannedBoardState) {
        if (cost != null)
            cost.pay(source, plannedBoardState);
        effect.resolve(source, plannedBoardState);
    }

    @Override
    public boolean canPayCost(BotCard source, PlannedBoardState plannedBoardState) {
        if (cost == null) return true;
        return cost.canPayCost(source, plannedBoardState);
    }

    @Override
    public double getValueIfUsed(BotCard source, PlannedBoardState plannedBoardState) {
        double costValue = cost != null ? cost.getValueIfPayed(source, plannedBoardState) : 0.0;
        return effect.getValueIfResolved(source, plannedBoardState) + costValue;
    }
}
