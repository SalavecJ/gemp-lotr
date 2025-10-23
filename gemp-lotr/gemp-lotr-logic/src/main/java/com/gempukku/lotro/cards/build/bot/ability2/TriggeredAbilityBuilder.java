package com.gempukku.lotro.cards.build.bot.ability2;
import com.gempukku.lotro.cards.build.bot.ability2.cost.Cost;
import com.gempukku.lotro.cards.build.bot.ability2.effect.Effect;
import com.gempukku.lotro.cards.build.bot.ability2.trigger.Trigger;


public class TriggeredAbilityBuilder {
    private Boolean optional;
    private Trigger trigger;
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
        return new TriggeredAbility(optional, trigger, effect, cost);
    }
}
