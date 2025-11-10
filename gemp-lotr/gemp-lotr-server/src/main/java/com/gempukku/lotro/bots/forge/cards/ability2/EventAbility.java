package com.gempukku.lotro.bots.forge.cards.ability2;

import com.gempukku.lotro.bots.forge.cards.ability2.cost.Cost;
import com.gempukku.lotro.bots.forge.cards.ability2.cost.CostWithTarget;
import com.gempukku.lotro.bots.forge.cards.ability2.effect.Effect;
import com.gempukku.lotro.bots.forge.cards.ability2.effect.EffectWithTarget;
import com.gempukku.lotro.bots.forge.cards.abstractcard.BotCard;
import com.gempukku.lotro.bots.forge.plan.PlannedBoardState;

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
    public void resolveAbilityWithCostTarget(String player, PlannedBoardState plannedBoardState, BotCard costTarget) {
        if (cost != null && cost instanceof CostWithTarget costWithTarget) {
            costWithTarget.payWithTarget(player, plannedBoardState, costTarget);
            effect.resolve(player, plannedBoardState);
        } else {
            if (cost == null)
                throw new IllegalStateException("No cost to target for paying with target.");
            throw new IllegalStateException("Cost targeting for this cost not supported: " + cost.getClass().getSimpleName());
        }
    }

    @Override
    public void resolveAbilityOnTarget(String player, PlannedBoardState plannedBoardState, BotCard target) {
        if (effect instanceof EffectWithTarget effectWithTarget) {
            if (cost != null)
                cost.pay(player, plannedBoardState);
            effectWithTarget.resolveWithTarget(player, plannedBoardState, target);
        } else {
            throw new IllegalStateException("Targeting for this effect not supported: " + effect.getClass().getSimpleName());
        }
    }

    @Override
    public void resolveAbilityOnTargetWithCostTarget(String player, PlannedBoardState plannedBoardState, BotCard effectTarget, BotCard costTarget) {
        if (effect instanceof EffectWithTarget effectWithTarget) {
            if (cost != null && cost instanceof CostWithTarget costWithTarget) {
                costWithTarget.payWithTarget(player, plannedBoardState, costTarget);
                effectWithTarget.resolveWithTarget(player, plannedBoardState, effectTarget);
            } else {
                if (cost == null)
                    throw new IllegalStateException("No cost to target for paying with target.");
                throw new IllegalStateException("Cost targeting for this cost not supported: " + cost.getClass().getSimpleName());
            }
        } else {
            throw new IllegalStateException("Targeting for this effect not supported: " + effect.getClass().getSimpleName());
        }
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

    @Override
    public double getValueIfUsedOnTarget(String player, PlannedBoardState plannedBoardState, BotCard target) {
        if (effect instanceof EffectWithTarget effectWithTarget) {
            double costValue = cost != null ? cost.getValueIfPayed(player, plannedBoardState) : 0.0;
            return effectWithTarget.getValueIfResolvedWithTarget(player, plannedBoardState, target) + costValue;
        } else {
            throw new IllegalStateException("Targeting for this effect not supported: " + effect.getClass().getSimpleName());
        }
    }
}
