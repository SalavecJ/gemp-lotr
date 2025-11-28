package com.gempukku.lotro.bots.forge.plan.action2;

import com.gempukku.lotro.bots.forge.cards.abstractcard.BotCard;
import com.gempukku.lotro.game.PhysicalCard;

public abstract class ChooseCardAction2 extends ActionToTake2 {
    private final BotCard card;
    private final String actionId;

    public ChooseCardAction2(String decisionText, BotCard card, String actionId) {
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
        return card.getSelf();
    }

    @Override
    public abstract String toString();
}
