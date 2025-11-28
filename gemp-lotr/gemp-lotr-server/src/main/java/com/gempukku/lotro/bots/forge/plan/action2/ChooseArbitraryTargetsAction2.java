package com.gempukku.lotro.bots.forge.plan.action2;

import com.gempukku.lotro.bots.forge.cards.abstractcard.BotCard;

import java.util.List;

public class ChooseArbitraryTargetsAction2 extends ChooseTargetsAction2 {
    private final List<String> tempIds;

    public ChooseArbitraryTargetsAction2(String decisionText, BotCard source, List<BotCard> targets, List<String> tempIds) {
        super(decisionText, source, targets);
        this.tempIds = tempIds;
    }

    @Override
    public String carryOut() {
        return String.join(",", tempIds);
    }
}
