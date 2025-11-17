package com.gempukku.lotro.bots.forge.plan.action;

import com.gempukku.lotro.bots.forge.cards.ability2.cost.CostWithTarget;
import com.gempukku.lotro.bots.forge.cards.abstractcard.BotCard;

import java.util.List;
import java.util.stream.Collectors;

public class ChooseTargetsForCostAction extends ChooseTargetsAction {
    private final BotCard source;
    private final CostWithTarget cost;

    public ChooseTargetsForCostAction(List<BotCard> targets, BotCard source, CostWithTarget cost) {
        super(targets);
        this.source = source;
        this.cost = cost;
    }

    public BotCard getSource() {
        return source;
    }

    public CostWithTarget getCost() {
        return cost;
    }

    @Override
    public String toString() {
        String joined = getTargets().stream()
                .map(t -> t.getSelf().getBlueprint().getFullName())
                .sorted()
                .collect(Collectors.joining("; "));
        return "Action: Choose " + joined + " to pay cost of " + source.getSelf().getBlueprint().getFullName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChooseTargetsForCostAction that = (ChooseTargetsForCostAction) o;
        if (source.getSelf().getCardId() != that.source.getSelf().getCardId()) return false;
        if (getTargets().size() != that.getTargets().size()) return false;

        List<Integer> thisIds = getTargets().stream()
                .map(t -> t.getSelf().getCardId())
                .sorted()
                .toList();
        List<Integer> thatIds = that.getTargets().stream()
                .map(t -> t.getSelf().getCardId())
                .sorted()
                .toList();
        return thisIds.equals(thatIds);
    }

    @Override
    public int hashCode() {
        int result = Integer.hashCode(source.getSelf().getCardId());
        for (BotCard target : getTargets()) {
            result = 31 * result + Integer.hashCode(target.getSelf().getCardId());
        }
        return result;
    }
}
