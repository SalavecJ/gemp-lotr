package com.gempukku.lotro.cards.build.bot.ability2;
import com.gempukku.lotro.cards.build.bot.ability2.cost.Cost;
import com.gempukku.lotro.cards.build.bot.ability2.effect.Effect;


public class EventAbilityBuilder {
    private Cost cost = null;
    private Effect effect = null;

    public EventAbilityBuilder() {
    }

    public EventAbilityBuilder cost(Cost cost) {
        if (this.cost != null) {
            throw new IllegalStateException("Already has a cost");
        }
        this.cost = cost;
        return this;
    }

    public EventAbilityBuilder effect(Effect effect) {
        if (this.effect != null) {
            throw new IllegalStateException("Already has an effect");
        }
        this.effect = effect;
        return this;
    }

    public EventAbility build() {
        return new EventAbility(effect, cost);
    }
}
