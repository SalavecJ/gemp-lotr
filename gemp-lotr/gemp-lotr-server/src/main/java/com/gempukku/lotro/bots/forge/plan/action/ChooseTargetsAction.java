package com.gempukku.lotro.bots.forge.plan.action;

import com.gempukku.lotro.bots.forge.cards.abstractcards.BotCard;

import java.util.List;
import java.util.stream.Collectors;

public class ChooseTargetsAction extends ActionToTake {
    protected final BotCard source;
    protected final List<BotCard> targets;
    protected final List<String> targetIds;

    public ChooseTargetsAction(String decisionText, BotCard source, List<BotCard> targets) {
        super(decisionText);
        this.source = source;
        this.targets = targets;
        this.targetIds = targets.stream().map(card -> String.valueOf(card.getPhysicalCard().getCardId())).toList();
    }

    public BotCard getSource() {
        return source;
    }

    public List<BotCard> getTargets() {
        return targets;
    }

    public boolean isAttachAction() {
        return getDecisionText().startsWith("Attach ");
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
        return "Action: Choose " + joined + " for " + source.getFullName();
    }
}
