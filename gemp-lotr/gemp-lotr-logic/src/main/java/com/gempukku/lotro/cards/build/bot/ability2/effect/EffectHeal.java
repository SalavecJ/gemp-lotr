package com.gempukku.lotro.cards.build.bot.ability2.effect;

import com.gempukku.lotro.cards.build.bot.BotTargetingMode;
import com.gempukku.lotro.cards.build.bot.abstractcard.BotCard;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.game.state.PlannedBoardState;

import java.util.List;
import java.util.function.Predicate;

public class EffectHeal extends EffectWithTarget {
    protected final Predicate<BotCard> targetPredicate;
    protected final int amount;

    public EffectHeal(Predicate<BotCard> targetPredicate, int amount) {
        this.targetPredicate = targetPredicate;
        this.amount = amount;
    }

    @Override
    public List<BotCard> getPotentialTargets(PlannedBoardState plannedBoardState) {
        return plannedBoardState.getActiveCards().stream().filter(targetPredicate).toList();
    }

    @Override
    public boolean affectsAll() {
        return false;
    }

    @Override
    public BotCard chooseTarget(PlannedBoardState plannedBoardState) {
        if (getPotentialTargets(plannedBoardState).isEmpty()) {
            return null;
        }
        return BotTargetingMode.HEAL.chooseTarget(plannedBoardState, getPotentialTargets(plannedBoardState), false);
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public void resolve(BotCard source, PlannedBoardState plannedBoardState) {
        BotCard target = chooseTarget(plannedBoardState);
        if (target == null) return;
        plannedBoardState.heal(target, amount);
    }

    @Override
    public double getValueIfResolved(BotCard source, PlannedBoardState plannedBoardState) {
        BotCard toBeHealed = chooseTarget(plannedBoardState);
        if (toBeHealed == null) return 0.0;

        double value = Math.min(this.amount, plannedBoardState.getVitality(toBeHealed) - 1);
        // healing an ally has lower impact
        if (toBeHealed.getSelf().getBlueprint().getCardType().equals(CardType.ALLY)) {
            value /= 2.0;
        }
        // healing an exhausted companion has higher impact
        if (toBeHealed.getSelf().getBlueprint().getCardType().equals(CardType.COMPANION)
                && plannedBoardState.getVitality(toBeHealed) == 1) {
            value += 0.5;
        }
        // healing my own cards is positive value, opposite for opponent's cards
        return toBeHealed.getSelf().getOwner().equals(source.getSelf().getOwner()) ? value : -value;
    }
}
