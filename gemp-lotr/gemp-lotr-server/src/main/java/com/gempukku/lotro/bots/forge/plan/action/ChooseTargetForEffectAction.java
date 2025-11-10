package com.gempukku.lotro.bots.forge.plan.action;

import com.gempukku.lotro.bots.forge.cards.ability2.effect.EffectWithTarget;
import com.gempukku.lotro.bots.forge.cards.abstractcard.BotCard;

public class ChooseTargetForEffectAction extends ChooseTargetAction {
    private final BotCard source;
    private final EffectWithTarget effect;

    public ChooseTargetForEffectAction(BotCard target, BotCard source, EffectWithTarget effect) {
        super(target);
        this.source = source;
        this.effect = effect;
    }

    public BotCard getSource() {
        return source;
    }

    public EffectWithTarget getEffect() {
        return effect;
    }

    @Override
    public String toString() {
        return "Action: Choose " + getTarget().getSelf().getBlueprint().getFullName() + " for effect of " + source.getSelf().getBlueprint().getFullName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChooseTargetForEffectAction that = (ChooseTargetForEffectAction) o;
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
