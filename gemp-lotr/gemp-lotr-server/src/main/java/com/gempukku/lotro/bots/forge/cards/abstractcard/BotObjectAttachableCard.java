package com.gempukku.lotro.bots.forge.cards.abstractcard;

import com.gempukku.lotro.bots.forge.cards.BotTargetingMode;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.List;

public abstract class BotObjectAttachableCard extends BotObjectCard {
    public BotObjectAttachableCard(PhysicalCard self) {
        super(self);
    }

    @Override
    protected boolean playsToSupportArea() {
        return false;
    }

    public BotCard chooseTargetToAttachTo(DefaultLotroGame game, List<BotCard> possibleTargets) {
        return getAttachTargetingMode().chooseTarget(game, possibleTargets, false);
    }

    protected abstract BotTargetingMode getAttachTargetingMode();
}
