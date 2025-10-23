package com.gempukku.lotro.cards.build.bot.ability2;

import com.gempukku.lotro.cards.build.bot.ability2.effect.Effect;
import com.gempukku.lotro.cards.build.bot.ability2.cost.Cost;
import com.gempukku.lotro.cards.build.bot.ability2.effect.EffectWithTarget;
import com.gempukku.lotro.cards.build.bot.ability2.trigger.Trigger;
import com.gempukku.lotro.cards.build.bot.abstractcard.BotCard;
import com.gempukku.lotro.game.state.PlannedBoardState;

public class TriggeredAbility implements Ability {
    protected final boolean optionalTrigger;
    protected final Trigger trigger;
    protected final Effect effect;
    protected final Cost cost;

    protected TriggeredAbility(boolean optionalTrigger, Trigger trigger, Effect effect, Cost cost) {
        this.optionalTrigger = optionalTrigger;
        this.trigger = trigger;
        this.effect = effect;
        this.cost = cost;
    }

    public boolean isOptionalTrigger() {
        return optionalTrigger;
    }

    public Trigger getTrigger() {
        return trigger;
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
    public double getValueIfUsedOnTarget(String player, PlannedBoardState plannedBoardState, BotCard target) {
        if (effect instanceof EffectWithTarget effectWithTarget) {
            double costValue = cost != null ? cost.getValueIfPayed(player, plannedBoardState) : 0.0;
            return effectWithTarget.getValueIfResolvedWithTarget(player, plannedBoardState, target) + costValue;
        } else {
            throw new IllegalStateException("Targeting for this effect not supported: " + effect.getClass().getSimpleName());
        }
    }

    @Override
    public boolean conditionOk(String player, PlannedBoardState plannedBoardState) {
        return true;
    }
}
