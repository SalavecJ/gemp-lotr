package com.gempukku.lotro.bots.forge.plan.action;

import com.gempukku.lotro.bots.forge.cards.abstractcards.BotCard;

public class DiscardCompanionToHealAction extends ChooseCardAction {

    public DiscardCompanionToHealAction(String decisionText, BotCard toDiscard, String actionId) {
        super(decisionText, toDiscard, actionId);
    }

    @Override
    public String toString() {
        return "Action: Discard " + getPhysicalCard().getBlueprint().getFullName() + " from hand to heal companion in play";
    }
}
