package com.gempukku.lotro.bots.forge.cards.ability2;
import com.gempukku.lotro.bots.forge.cards.ability2.condition.Condition;
import com.gempukku.lotro.bots.forge.cards.ability2.cost.Cost;
import com.gempukku.lotro.bots.forge.cards.ability2.effect.Effect;
import com.gempukku.lotro.bots.forge.cards.ability2.trigger.Trigger;

import java.util.ArrayList;
import java.util.List;


public class TriggeredAbilityBuilder {
    private Boolean optional;
    private Trigger trigger;
    private List<Condition> conditions = new ArrayList<>();
    private Effect effect;
    private Cost cost;

    public TriggeredAbilityBuilder() {
    }

    public TriggeredAbilityBuilder trigger(Trigger trigger) {
        if (this.trigger != null) {
            throw new IllegalStateException("Already has a trigger");
        }
        this.trigger = trigger;
        return this;
    }

    public TriggeredAbilityBuilder cost(Cost cost) {
        if (this.cost != null) {
            throw new IllegalStateException("Already has a cost");
        }
        this.cost = cost;
        return this;
    }

    public TriggeredAbilityBuilder condition(Condition condition) {
        conditions.add(condition);
        return this;
    }


    public TriggeredAbilityBuilder effect(Effect effect) {
        if (this.effect != null) {
            throw new IllegalStateException("Already has an effect");
        }
        this.effect = effect;
        return this;
    }

    public TriggeredAbilityBuilder optional(boolean optional) {
        if (this.optional != null) {
            throw new IllegalStateException("Already has optional set");
        }
        this.optional = optional;
        return this;
    }

    public TriggeredAbility build() {
        if (trigger == null) {
            throw new IllegalStateException("Triggered abilities must have trigger");
        }
        if (optional == null) {
            throw new IllegalStateException("Triggered abilities must be explicitly set as optional or not");
        }
        if (effect == null) {
            throw new IllegalStateException("Triggered abilities must have effect");
        }
        return new TriggeredAbility(optional, trigger, conditions, effect, cost);
    }
}
