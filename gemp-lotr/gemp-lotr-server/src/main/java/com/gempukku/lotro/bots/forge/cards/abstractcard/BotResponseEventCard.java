package com.gempukku.lotro.bots.forge.cards.abstractcard;

import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.PhysicalCard;

public abstract class BotResponseEventCard extends BotEventCard {
    public BotResponseEventCard(PhysicalCard self) {
        super(self);
        if (!self.getBlueprint().hasTimeword(Timeword.RESPONSE)) {
            throw new IllegalArgumentException("This is not a response event: " + self.getBlueprint().getTitle());
        }
    }
}
