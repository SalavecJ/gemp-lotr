package com.gempukku.lotro.bots.forge.cards.abstractcard;

import com.gempukku.lotro.bots.forge.cards.TriggerCondition;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.PhysicalCard;

import java.util.Set;

public abstract class BotResponseEventCard extends BotEventCard {
    public BotResponseEventCard(PhysicalCard self) {
        super(self);
        if (!self.getBlueprint().hasTimeword(Timeword.RESPONSE)) {
            throw new IllegalArgumentException("This is not a response event: " + self.getBlueprint().getTitle());
        }
    }


    /**
     * Returns the trigger(s) that allow this card to be played
     */
    public abstract Set<TriggerCondition> getTriggerConditions();
}
