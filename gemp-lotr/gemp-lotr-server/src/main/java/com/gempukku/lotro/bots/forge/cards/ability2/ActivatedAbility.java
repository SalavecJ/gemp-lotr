package com.gempukku.lotro.bots.forge.cards.ability2;

import com.gempukku.lotro.bots.forge.cards.ability2.condition.Condition;
import com.gempukku.lotro.bots.forge.cards.ability2.cost.Cost;
import com.gempukku.lotro.bots.forge.cards.ability2.effect.Effect;
import com.gempukku.lotro.common.Phase;

import java.util.List;

public class ActivatedAbility extends Ability {
    protected final Phase phase;

    public ActivatedAbility(Phase phase, List<Condition> conditions, Effect effect, Cost cost) {
        super(effect, cost, conditions);
        this.phase = phase;
    }

    public Phase getPhase() {
        return phase;
    }
}
