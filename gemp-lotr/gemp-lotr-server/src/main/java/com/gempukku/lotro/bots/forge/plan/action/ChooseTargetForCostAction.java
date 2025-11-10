package com.gempukku.lotro.bots.forge.plan.action;

import com.gempukku.lotro.bots.forge.cards.ability2.cost.CostWithTarget;
import com.gempukku.lotro.bots.forge.cards.abstractcard.BotCard;

public class ChooseTargetForCostAction extends ChooseTargetAction {
    private final BotCard source;
    private final CostWithTarget cost;

    public ChooseTargetForCostAction(BotCard target, BotCard source, CostWithTarget cost) {
        super(target);
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
        return "Action: Choose " + getTarget().getSelf().getBlueprint().getFullName() + " to pay cost of " + source.getSelf().getBlueprint().getFullName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChooseTargetForCostAction that = (ChooseTargetForCostAction) o;
        return getTarget().getSelf().getCardId() == that.getTarget().getSelf().getCardId()
                && source.getSelf().getCardId() == that.source.getSelf().getCardId();
    }

    @Override
    public int hashCode() {
        int result = Integer.hashCode(getTarget().getSelf().getCardId());
        result = 31 * result + Integer.hashCode(source.getSelf().getCardId());
        return result;
    }
}
