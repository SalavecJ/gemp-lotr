package com.gempukku.lotro.cards.build.bot.ability2.effect;

import com.gempukku.lotro.cards.build.bot.abstractcard.BotAllyCard;
import com.gempukku.lotro.cards.build.bot.abstractcard.BotCard;
import com.gempukku.lotro.cards.build.bot.abstractcard.BotCompanionCard;
import com.gempukku.lotro.game.state.PlannedBoardState;

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
        if (target instanceof BotCompanionCard companionCard) {
            plannedBoardState.playCompanion(companionCard, amount);
        } else if (target instanceof BotAllyCard allyCard) {
            plannedBoardState.playToFpSupportArea(allyCard, amount);
        } else {
            throw new IllegalStateException("Unsupported card type for EffectPlayWithBonusTwilightModification: " + target.getSelf().getBlueprint().getFullName());
        }
    }

    @Override
    public double getValueIfResolvedWithTarget(String player, PlannedBoardState plannedBoardState, BotCard target) {
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
