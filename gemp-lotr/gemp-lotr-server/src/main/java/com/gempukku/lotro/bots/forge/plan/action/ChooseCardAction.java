package com.gempukku.lotro.bots.forge.plan.action;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

public abstract class ChooseCardAction implements ActionToTake {
    private final PhysicalCard card;

    public ChooseCardAction(PhysicalCard card) {
        this.card = card;
    }

    @Override
    public int carryOut(AwaitingDecision awaitingDecision) {
        for (int i = 0; i < awaitingDecision.getDecisionParameters().get("cardId").length; i++) {
            if (Integer.parseInt(awaitingDecision.getDecisionParameters().get("cardId")[i]) == card.getCardId()) {
                return i;
            }
        }
        throw new IllegalArgumentException("Could not find predetermined action in decision options: " + awaitingDecision.toJson().toString());
    }

    protected PhysicalCard getCard() {
        return card;
    }

    @Override
    public abstract String toString();
}
