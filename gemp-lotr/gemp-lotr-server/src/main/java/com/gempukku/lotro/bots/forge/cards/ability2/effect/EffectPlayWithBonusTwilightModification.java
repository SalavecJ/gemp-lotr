package com.gempukku.lotro.bots.forge.cards.ability2.effect;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.List;
import java.util.function.Predicate;

public class EffectPlayWithBonusTwilightModification extends EffectPlayWithBonus {
    private final int amount;

    public EffectPlayWithBonusTwilightModification(Predicate<PhysicalCard> targetPredicate, int amount) {
        super(targetPredicate);
        this.amount = amount;
    }


    @Override
    protected double getValueIfResolvedOn(String player, DefaultLotroGame game, PhysicalCard target) {
        return 0.4 * amount * -1;
    }

    @Override
    public String toString(String player, DefaultLotroGame game, List<PhysicalCard> targets) {
        if (targets.isEmpty()) {
            return "attempt to play card from hand with modified twilight, but none can be chosen";
        } else if (targets.size() == 1) {
            return "play " + targets.getFirst().getBlueprint().getFullName() + " from hand with modified twilight: " + amount;
        } else {
            throw new IllegalStateException("EffectPlayWithBonusTwilightModification cannot be applied to multiple targets");
        }
    }
}
