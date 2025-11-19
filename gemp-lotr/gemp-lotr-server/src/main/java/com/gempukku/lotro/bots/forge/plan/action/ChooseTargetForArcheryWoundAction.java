package com.gempukku.lotro.bots.forge.plan.action;

import com.gempukku.lotro.bots.forge.cards.abstractcard.BotCard;

import java.util.List;

public class ChooseTargetForArcheryWoundAction extends ChooseTargetsAction {

    public ChooseTargetForArcheryWoundAction(BotCard target) {
        super(List.of(target));
    }

    public BotCard getTarget() {
        return super.getTargets().getFirst();
    }

    @Override
    public String toString() {
        return "Action: Choose " + getTarget().getSelf().getBlueprint().getFullName() + " for archery wound";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChooseTargetForArcheryWoundAction that = (ChooseTargetForArcheryWoundAction) o;
        return getTarget().getSelf().getCardId() == that.getTarget().getSelf().getCardId();
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(getTarget().getSelf().getCardId());
    }
}
