package com.gempukku.lotro.bots.forge.plan.action;

import com.gempukku.lotro.bots.forge.cards.abstractcards.BotCard;

public class PlayCardFromHandAction extends ChooseCardAction {

    public PlayCardFromHandAction(String decisionText, BotCard toPlay, String actionId) {
        super(decisionText, toPlay, actionId);
    }

    @Override
    public String toString() {
        return "Action: Play " + getPhysicalCard().getBlueprint().getFullName() + " from hand";
    }
}
