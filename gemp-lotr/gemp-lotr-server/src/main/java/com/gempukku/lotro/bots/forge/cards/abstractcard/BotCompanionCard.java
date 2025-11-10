package com.gempukku.lotro.bots.forge.cards.abstractcard;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.bots.forge.plan.PlannedBoardState;

public abstract class BotCompanionCard extends BotCharacterCard {
    public BotCompanionCard(PhysicalCard self) {
        super(self);
    }

    @Override
    public boolean canBePlayedNoMatterThePhase(PlannedBoardState plannedBoardState) {
        return super.canBePlayedNoMatterThePhase(plannedBoardState) && ruleOfNineOk(plannedBoardState);
    }

    private boolean ruleOfNineOk(PlannedBoardState plannedBoardState) {
        return plannedBoardState.ruleOfNineRemainder(self.getOwner()) > 0;
    }
}
