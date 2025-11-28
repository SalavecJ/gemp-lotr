package com.gempukku.lotro.bots.forge.cards.abstractcard;

import com.gempukku.lotro.bots.forge.cards.ability2.EventAbility;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

public abstract class BotEventCard extends BotCard {
    public BotEventCard(PhysicalCard self) {
        super(self);
    }

    @Override
    public boolean canBePlayedNoMatterThePhase(DefaultLotroGame game) {
        return (getEventAbility().canPayCost(this.getSelf().getOwner(), game));
    }

    public boolean isResponseEvent() {
        return self.getBlueprint().hasTimeword(Timeword.RESPONSE);
    }

    @Override
    public abstract EventAbility getEventAbility();
}
