package com.gempukku.lotro.bots.forge.cards.ability2.effect;

import com.gempukku.lotro.bots.forge.cards.abstractcard.BotCard;
import com.gempukku.lotro.bots.forge.plan.PlannedBoardState;

import java.util.List;
import java.util.function.Predicate;

public class EffectPlayWithBonusTwilightModification extends EffectPlayWithBonus {
    private final int amount;

    public EffectPlayWithBonusTwilightModification(Predicate<BotCard> targetPredicate, int amount) {
        super(targetPredicate);
        this.amount = amount;
    }

    @Override
    public void resolveWithTarget(String player, PlannedBoardState plannedBoardState, BotCard target) {
        plannedBoardState.playCard(target, amount);
    }

    @Override
    public double getValueIfResolvedWithTarget(String player, PlannedBoardState plannedBoardState, BotCard target) {
        return 0.4 * amount * -1;
    }

    @Override
    public double getValueIfResolved(String player, PlannedBoardState plannedBoardState) {
        return 0.4 * amount * -1;
    }

    @Override
    public String toString(String player, PlannedBoardState plannedBoardState, List<BotCard> targets) {
        if (targets.isEmpty()) {
            return "attempt to play card from hand with modified twilight, but none can be chosen";
        } else if (targets.size() == 1) {
            return "play " + targets.getFirst().getSelf().getBlueprint().getFullName() + " from hand with modified twilight: " + amount;
        } else {
            throw new IllegalStateException("EffectPlayWithBonusTwilightModification cannot be applied to multiple targets");
        }
    }
}
