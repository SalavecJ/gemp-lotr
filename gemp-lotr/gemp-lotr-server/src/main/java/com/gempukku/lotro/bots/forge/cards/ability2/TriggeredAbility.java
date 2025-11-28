package com.gempukku.lotro.bots.forge.cards.ability2;

import com.gempukku.lotro.bots.forge.cards.ability2.effect.Effect;
import com.gempukku.lotro.bots.forge.cards.ability2.cost.Cost;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.function.BiFunction;

public class TriggeredAbility extends Ability {
    protected final boolean optionalTrigger;
    private final BiFunction<String, DefaultLotroGame, Boolean> goodToUseFunction;

    protected TriggeredAbility(boolean optionalTrigger, Effect effect, Cost cost,
                               BiFunction<String, DefaultLotroGame, Boolean> goodToUseFunction) {
        super(effect, cost);
        this.optionalTrigger = optionalTrigger;
        this.goodToUseFunction = goodToUseFunction;
    }

    public boolean isOptionalTrigger() {
        return optionalTrigger;
    }

    public boolean goodToUse(String player, DefaultLotroGame game) {
        return goodToUseFunction.apply(player, game);
    }
}
