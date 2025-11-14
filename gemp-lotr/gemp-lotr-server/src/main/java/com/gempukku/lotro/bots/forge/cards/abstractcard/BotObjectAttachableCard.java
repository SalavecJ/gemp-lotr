package com.gempukku.lotro.bots.forge.cards.abstractcard;

import com.gempukku.lotro.bots.forge.cards.BotTargetingMode;
import com.gempukku.lotro.bots.forge.plan.PlannedBoardState;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

import java.util.List;

public abstract class BotObjectAttachableCard extends BotObjectCard {
    public BotObjectAttachableCard(PhysicalCard self) {
        super(self);
    }

    @Override
    protected boolean playsToSupportArea() {
        return false;
    }

    @Override
    public BotTargetingMode getTargetingModeForDecision(LotroGame game, AwaitingDecision decision) {
        PhysicalCard fromDecision = game.getGameState().getPhysicalCard(
                Integer.parseInt(decision.getDecisionParameters().get("source")[0])
        );
        if (!fromDecision.equals(self)) {
            throw new IllegalStateException("Wrong bot card implementation selected for: " + decision.toJson().toString()
                    + "; Selected: " + self.getBlueprint().getFullName() + "; Decision card: " + fromDecision.getBlueprint().getFullName());
        }

        if (decision.getText().equals("Attach " + self.getBlueprint().getFullName() + ". Choose target to attach to")) {
            return getAttachTargetingMode();
        }

        return super.getTargetingModeForDecision(game, decision);
    }

    public BotCard chooseTargetToAttachTo(PlannedBoardState plannedBoardState, List<BotCard> possibleTargets) {
        return getAttachTargetingMode().chooseTarget(plannedBoardState, possibleTargets, false);
    }

    protected abstract BotTargetingMode getAttachTargetingMode();
}
