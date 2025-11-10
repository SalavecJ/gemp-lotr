package com.gempukku.lotro.bots.forge.plan.action;

import com.gempukku.lotro.bots.forge.cards.abstractcard.BotCard;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

public abstract class ChooseCardAction implements ActionToTake {
    private final BotCard card;

    public ChooseCardAction(BotCard card) {
        this.card = card;
    }

    @Override
    public int carryOut(AwaitingDecision awaitingDecision) {
        for (int i = 0; i < awaitingDecision.getDecisionParameters().get("cardId").length; i++) {
            if (Integer.parseInt(awaitingDecision.getDecisionParameters().get("cardId")[i]) == card.getSelf().getCardId()
                    && awaitingDecision.getDecisionParameters().get("actionText")[i].startsWith(actionPrefix())) {
                return i;
            }
        }
        throw new IllegalArgumentException("Could not find predetermined action in decision options: " + awaitingDecision.toJson().toString());
    }

    protected abstract String actionPrefix();

    public BotCard getCard() {
        return card;
    }

    public PhysicalCard getPhysicalCard() {
        return card.getSelf();
    }

    @Override
    public abstract String toString();
}
