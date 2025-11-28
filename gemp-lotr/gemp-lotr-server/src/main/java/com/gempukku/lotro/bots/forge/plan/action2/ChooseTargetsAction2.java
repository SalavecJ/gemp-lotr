package com.gempukku.lotro.bots.forge.plan.action2;

import com.gempukku.lotro.bots.forge.cards.abstractcard.BotCard;

import java.util.List;
import java.util.stream.Collectors;

public class ChooseTargetsAction2 extends ActionToTake2 {
    protected final BotCard source;
    protected final List<BotCard> targets;
    protected final List<String> targetIds;

    public ChooseTargetsAction2(String decisionText, BotCard source, List<BotCard> targets) {
        super(decisionText);
        this.source = source;
        this.targets = targets;
        this.targetIds = targets.stream().map(card -> String.valueOf(card.getSelf().getCardId())).toList();
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
                .map(t -> t.getSelf().getBlueprint().getFullName())
                .sorted()
                .collect(Collectors.joining("; "));
        return "Action: Choose " + joined + " for " + source.getSelf().getBlueprint().getFullName();
    }
}
