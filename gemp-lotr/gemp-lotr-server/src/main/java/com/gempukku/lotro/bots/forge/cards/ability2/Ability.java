package com.gempukku.lotro.bots.forge.cards.ability2;

import com.gempukku.lotro.bots.forge.cards.ability2.cost.Cost;
import com.gempukku.lotro.bots.forge.cards.ability2.cost.CostWithTarget;
import com.gempukku.lotro.bots.forge.cards.ability2.effect.Effect;
import com.gempukku.lotro.bots.forge.cards.ability2.effect.EffectWithTarget;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

public abstract class Ability {
    protected final Effect effect;
    protected final Cost cost;

    public Ability(Effect effect, Cost cost) {
        this.effect = effect;
        this.cost = cost;
    }

    public final Effect getEffect() {
        return effect;
    }

    public final Cost getCost() {
        return cost;
    }

    public final boolean canPayCost(String player, DefaultLotroGame game) {
        if (cost == null) return true;
        return cost.canPayCost(player, game);
    }

    public final double getPossibleValue(String player, DefaultLotroGame game) {
        double tbr = 0.0;
        if (cost != null) {
            if (cost instanceof CostWithTarget costWithTarget) {
                tbr += costWithTarget.getMaximumPossibleValue(player, game);
            } else {
                tbr += cost.getValueIfPayed(player, game);
            }
        }
        if (effect instanceof EffectWithTarget effectWithTarget) {
            tbr += effectWithTarget.getMaximumPossibleValue(player, game);
        } else {
            tbr += effect.getValueIfResolved(player, game);
        }
        return tbr;
    }
}
