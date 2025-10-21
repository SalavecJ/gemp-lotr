package com.gempukku.lotro.cards.build.bot.ability2.effect;

import com.gempukku.lotro.cards.build.bot.BotTargetingMode;
import com.gempukku.lotro.cards.build.bot.abstractcard.BotCard;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.game.state.PlannedBoardState;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class EffectHeal extends EffectWithTarget {
    protected final Predicate<BotCard> targetPredicate;
    protected final int amount;

    public EffectHeal(Predicate<BotCard> targetPredicate, int amount) {
        this.targetPredicate = targetPredicate;
        this.amount = amount;
    }

    @Override
    public ArrayList<BotCard> getPotentialTargets(String player, PlannedBoardState plannedBoardState) {
        return new ArrayList<>(plannedBoardState.getActiveCards().stream()
                .filter(targetPredicate)
                .filter(botCard -> plannedBoardState.getWounds(botCard) > 0)
                .toList());
    }

    @Override
    public boolean affectsAll() {
        return false;
    }

    @Override
    public BotCard chooseTarget(String player, PlannedBoardState plannedBoardState) {
        if (getPotentialTargets(player, plannedBoardState).isEmpty()) {
            return null;
        }
        return BotTargetingMode.HEAL.chooseTarget(plannedBoardState, getPotentialTargets(player, plannedBoardState), false);
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public String toString(String player, PlannedBoardState plannedBoardState, List<BotCard> targets) {
        if (targets.isEmpty()) {
            return "attempt to heal a character, but none can be chosen";
        } else {
            String joined = targets.stream()
                    .map(t -> t.getSelf().getBlueprint().getFullName())
                    .collect(Collectors.joining("; "));
            if (amount == 1) {
                return "remove a wound from " + joined;
            } else {
                return "remove " + amount + "wounds from " + joined;
            }
        }
    }

    @Override
    public void resolve(String player, PlannedBoardState plannedBoardState) {
        BotCard target = chooseTarget(player, plannedBoardState);
        if (target == null) return;
        plannedBoardState.heal(target, amount);
    }

    @Override
    public double getValueIfResolved(String player, PlannedBoardState plannedBoardState) {
        BotCard toBeHealed = chooseTarget(player, plannedBoardState);
        if (toBeHealed == null) return 0.0;

        double value = Math.min(this.amount, plannedBoardState.getWounds(toBeHealed));
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
        return toBeHealed.getSelf().getOwner().equals(player) ? value : -value;
    }
}
