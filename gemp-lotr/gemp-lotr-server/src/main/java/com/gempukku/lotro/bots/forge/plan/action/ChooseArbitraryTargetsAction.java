package com.gempukku.lotro.bots.forge.plan.action;

import com.gempukku.lotro.bots.forge.cards.abstractcards.BotCard;

import java.util.List;

public class ChooseArbitraryTargetsAction extends ChooseTargetsAction {
    private final List<String> tempIds;

    public ChooseArbitraryTargetsAction(String decisionText, BotCard source, List<BotCard> targets, List<String> tempIds) {
        super(decisionText, source, targets);
        this.tempIds = tempIds;
    }

    @Override
    public String carryOut() {
        return String.join(",", tempIds);
    }
}
