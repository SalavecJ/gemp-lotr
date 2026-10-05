package com.gempukku.lotro.bots.forge.plan.action;

import com.gempukku.lotro.bots.forge.cards.abstractcards.BotCard;

public class ChooseIntegerAction extends ActionToTake {
    private final BotCard sourceCard;
    private final int value;

    public ChooseIntegerAction(String decisionText, BotCard sourceCard, int value) {
        super(decisionText);
        this.sourceCard = sourceCard;
        this.value = value;
    }

    @Override
    public String carryOut() {
        return String.valueOf(value);
    }

    @Override
    public String toString() {
        return "Action: Choose " + value + " for card " + sourceCard.getFullName();
    }
}
