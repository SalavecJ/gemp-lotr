package com.gempukku.lotro.bots.forge.cards.ability2;

import com.gempukku.lotro.bots.forge.cards.ability2.condition.Condition;
import com.gempukku.lotro.bots.forge.cards.ability2.cost.CostWithTarget;
import com.gempukku.lotro.bots.forge.cards.ability2.effect.Effect;
import com.gempukku.lotro.bots.forge.cards.ability2.cost.Cost;
import com.gempukku.lotro.bots.forge.cards.ability2.effect.EffectWithTarget;
import com.gempukku.lotro.bots.forge.cards.ability2.trigger.Trigger;
import com.gempukku.lotro.bots.forge.plan.PlannedBoardState;

public class TriggeredAbility extends Ability {
    protected final boolean optionalTrigger;
    protected final Trigger trigger;

    protected TriggeredAbility(boolean optionalTrigger, Trigger trigger, Condition condition, Effect effect, Cost cost) {
        super(effect, cost, condition);
        this.optionalTrigger = optionalTrigger;
        this.trigger = trigger;
    }

    public boolean isOptionalTrigger() {
        return optionalTrigger;
    }

    public Trigger getTrigger() {
        return trigger;
    }

    public boolean resolvesWithoutActionNeeded() {
        return !optionalTrigger
                && (cost == null || !(cost instanceof CostWithTarget))
                && !(effect instanceof EffectWithTarget);
    }

    public boolean goodToUseNoMatterWhat(String player, PlannedBoardState plannedBoardState) {
        if (cost != null) {
            return false;
        }
        if (effect instanceof EffectWithTarget effectWithTarget) {
            return effectWithTarget.getMinimumPossibleValue(player, plannedBoardState) >= 0;
        } else {
            return effect.getValueIfResolved(player, plannedBoardState) >= 0;
        }
    }
}
