package com.gempukku.lotro.bots.forge.plan.action;

import com.gempukku.lotro.bots.forge.cards.abstractcards.BotCard;

import java.util.List;
import java.util.stream.Collectors;

public class ForcedReconcileAction extends ActionToTake {
    protected final List<BotCard> targets;
    protected final List<String> targetIds;

    public ForcedReconcileAction(String decisionText, List<BotCard> targets) {
        super(decisionText);
        this.targets = targets;
        this.targetIds = targets.stream().map(card -> String.valueOf(card.getPhysicalCard().getCardId())).toList();
    }

    public List<BotCard> getTargets() {
        return targets;
    }

    @Override
    public String carryOut() {
        return String.join(",", targetIds);
    }

    @Override
    public String toString() {
        String joined = getTargets().stream()
                .map(BotCard::getFullName)
                .sorted()
                .collect(Collectors.joining("; "));
        return "Action: Choose " + joined + " to discard down to 8";
    }
}
