package com.gempukku.lotro.cards.build.bot.ability2.effect;

import com.gempukku.lotro.cards.build.bot.ability2.util.HandValueUtil;
import com.gempukku.lotro.cards.build.bot.abstractcard.BotCard;
import com.gempukku.lotro.game.state.PlannedBoardState;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class EffectPutFromHandToBottomOfDeck extends EffectWithTarget{
    protected final Predicate<BotCard> targetPredicate;

    public EffectPutFromHandToBottomOfDeck(Predicate<BotCard> targetPredicate) {
        this.targetPredicate = targetPredicate;
    }

    @Override
    public ArrayList<BotCard> getPotentialTargets(BotCard source, PlannedBoardState plannedBoardState) {
        return new ArrayList<>(plannedBoardState.getHand(source.getSelf().getOwner()).stream().filter(targetPredicate).toList());
    }

    @Override
    public boolean affectsAll() {
        return false;
    }

    @Override
    public BotCard chooseTarget(BotCard source, PlannedBoardState plannedBoardState) {
        List<BotCard> potentialTargets = getPotentialTargets(source, plannedBoardState);
        if (potentialTargets.isEmpty()) {
            return null;
        } else {
            potentialTargets.sort((o1, o2) -> Double.compare(getValueOfTarget(source, o2, plannedBoardState), getValueOfTarget(source, o1, plannedBoardState)));
            return potentialTargets.getFirst();
        }
    }

    @Override
    public void resolve(BotCard source, PlannedBoardState plannedBoardState) {
        BotCard target = chooseTarget(source, plannedBoardState);
        if (target == null) return;
        plannedBoardState.moveFromHandToBottomOfDeck(target);
    }

    @Override
    public double getValueIfResolved(BotCard source, PlannedBoardState plannedBoardState) {
        BotCard target = chooseTarget(source, plannedBoardState);
        return getValueOfTarget(source, target, plannedBoardState);
    }

    public double getValueOfTarget(BotCard source, BotCard target, PlannedBoardState plannedBoardState) {
        if (target == null) {
            return 0;
        }
        double targetValue = HandValueUtil.cardValueInHand(target, plannedBoardState);
        if (source.getSelf().getOwner().equals(target.getSelf().getOwner())){
            if (targetValue > 0) {
                return -targetValue;
            } else {
                return 0.5;
            }
        } else {
            if (targetValue > 0) {
                return targetValue;
            } else {
                return -0.5;
            }
        }
    }
}
