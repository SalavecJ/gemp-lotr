package com.gempukku.lotro.bots.forge.plan.action;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

public class DiscardCompanionToHealAction implements ActionToTake {
    private final PhysicalCard toDiscard;

    public DiscardCompanionToHealAction(PhysicalCard toDiscard) {
        this.toDiscard = toDiscard;
    }

    @Override
    public int carryOut(AwaitingDecision awaitingDecision) {
        for (int i = 0; i < awaitingDecision.getDecisionParameters().get("cardId").length; i++) {
            if (Integer.parseInt(awaitingDecision.getDecisionParameters().get("cardId")[i]) == toDiscard.getCardId()) {
                return i;
            }
        }
        throw new IllegalArgumentException("Could not find predetermined action in decision options: " + awaitingDecision.toJson().toString());
    }

    @Override
    public String toString() {
        return "Action: Discard " + toDiscard.getBlueprint().getFullName() + " from hand to heal companion in play";
    }
}
