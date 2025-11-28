package com.gempukku.lotro.bots.forge.cards.ability2;

import com.gempukku.lotro.bots.forge.cards.ability2.cost.Cost;
import com.gempukku.lotro.bots.forge.cards.ability2.effect.Effect;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.function.BiFunction;

public class TriggeredAbilityBuilder {
    private Boolean optional;
    private Effect effect;
    private Cost cost;
    private BiFunction<String, DefaultLotroGame, Boolean> goodToUseFunction;

    public TriggeredAbilityBuilder() {
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

    public TriggeredAbilityBuilder goodToUseFunction(BiFunction<String, DefaultLotroGame, Boolean> goodToUseFunction) {
        if (this.goodToUseFunction != null) {
            throw new IllegalStateException("Already has goodToUseFunction set");
        }
        this.goodToUseFunction = goodToUseFunction;
        return this;
    }

    public TriggeredAbility build() {
        if (optional == null) {
            throw new IllegalStateException("Triggered abilities must be explicitly set as optional or not");
        }
        if (effect == null) {
            throw new IllegalStateException("Triggered abilities must have effect");
        }
        if (goodToUseFunction == null && optional) {
            throw new IllegalStateException("Optional triggered abilities must have goodToUseFunction to be determined when to use");
        }
        if (goodToUseFunction != null && !optional) {
            throw new IllegalStateException("Non-optional triggered abilities cannot have goodToUseFunction");
        }
        return new TriggeredAbility(optional, effect, cost, goodToUseFunction);
    }
}
