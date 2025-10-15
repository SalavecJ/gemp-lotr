package com.gempukku.lotro.cards.build.bot.ability2;

import com.gempukku.lotro.cards.build.bot.TriggerCondition;
import com.gempukku.lotro.cards.build.bot.ability2.effect.Effect;
import com.gempukku.lotro.cards.build.bot.ability2.cost.Cost;

public class TriggeredAbility {
    protected final boolean optionalTrigger;
    protected final TriggerCondition triggerCondition;
    protected final Effect effect;
    protected final Cost cost;

    protected TriggeredAbility(boolean optionalTrigger, TriggerCondition triggerCondition, Effect effect, Cost cost) {
        this.optionalTrigger = optionalTrigger;
        this.triggerCondition = triggerCondition;
        this.effect = effect;
        this.cost = cost;
    }
}
