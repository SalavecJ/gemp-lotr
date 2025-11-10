package com.gempukku.lotro.bots.forge.plan.action;

import com.gempukku.lotro.bots.forge.cards.ability2.effect.Effect;
import com.gempukku.lotro.bots.forge.cards.abstractcard.BotCard;

public class UseCardAction extends ChooseCardAction {
    private final Class<? extends Effect> effectClass;

    public UseCardAction(BotCard toUse, Class<? extends Effect> effectClass) {
        super(toUse);
        this.effectClass = effectClass;
    }

    public Class<? extends Effect> getEffectClass() {
        return effectClass;
    }

    @Override
    protected String actionPrefix() {
        return "Use";
    }

    @Override
    public String toString() {
        return "Action: Use " + getPhysicalCard().getBlueprint().getFullName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UseCardAction that = (UseCardAction) o;
        return getPhysicalCard().getCardId() == that.getPhysicalCard().getCardId()
                && effectClass.equals(that.effectClass);
    }

    @Override
    public int hashCode() {
        int result = Integer.hashCode(getPhysicalCard().getCardId());
        result = 31 * result + effectClass.hashCode();
        return result;
    }
}
