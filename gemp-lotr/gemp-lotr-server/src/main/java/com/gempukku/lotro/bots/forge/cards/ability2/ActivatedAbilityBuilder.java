package com.gempukku.lotro.bots.forge.cards.ability2;
import com.gempukku.lotro.bots.forge.cards.ability2.condition.Condition;
import com.gempukku.lotro.bots.forge.cards.ability2.cost.Cost;
import com.gempukku.lotro.bots.forge.cards.ability2.effect.Effect;
import com.gempukku.lotro.common.Phase;


public class ActivatedAbilityBuilder {
    private Phase phase = null;
    private Condition condition = null;
    private Cost cost = null;
    private Effect effect = null;

    public ActivatedAbilityBuilder() {
    }

    public ActivatedAbilityBuilder phase(Phase phase) {
        if (this.phase != null) {
            throw new IllegalStateException("Already has a phase");
        }
        this.phase = phase;
        return this;
    }

    public ActivatedAbilityBuilder condition(Condition condition) {
        if (this.condition != null) {
            throw new IllegalStateException("Already has a condition");
        }
        this.condition = condition;
        return this;
    }

    public ActivatedAbilityBuilder cost(Cost cost) {
        if (this.cost != null) {
            throw new IllegalStateException("Already has a cost");
        }
        this.cost = cost;
        return this;
    }

    public ActivatedAbilityBuilder effect(Effect effect) {
        if (this.effect != null) {
            throw new IllegalStateException("Already has an effect");
        }
        this.effect = effect;
        return this;
    }

    public ActivatedAbility build() {
        if (phase == null) {
            throw new IllegalStateException("Activated abilities must have phase");
        }
        return new ActivatedAbility(phase, condition, effect, cost);
    }
}
