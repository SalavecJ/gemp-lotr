package com.gempukku.lotro.bots.forge.plan.action;

import com.gempukku.lotro.bots.forge.cards.abstractcards.BotCard;
import com.gempukku.lotro.game.PhysicalCard;

public abstract class ChooseCardAction extends ActionToTake {

    private final BotCard card;
    private final String actionId;

    public ChooseCardAction(String decisionText, BotCard card, String actionId) {
        super(decisionText);
        this.card = card;
        this.actionId = actionId;
    }

    @Override
    public String carryOut() {
        return actionId;
    }

    public BotCard getCard() {
        return card;
    }

    public PhysicalCard getPhysicalCard() {
        return card.getPhysicalCard();
    }
}
