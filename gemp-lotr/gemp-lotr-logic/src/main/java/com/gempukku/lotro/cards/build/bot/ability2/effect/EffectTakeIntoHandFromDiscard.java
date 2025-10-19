package com.gempukku.lotro.cards.build.bot.ability2.effect;

import com.gempukku.lotro.cards.build.bot.ability2.EventAbility;
import com.gempukku.lotro.cards.build.bot.abstractcard.BotCard;
import com.gempukku.lotro.cards.build.bot.abstractcard.BotEventCard;
import com.gempukku.lotro.game.state.PlannedBoardState;

import java.util.List;
import java.util.function.Predicate;

public class EffectTakeIntoHandFromDiscard extends EffectWithTarget{
    protected final Predicate<BotCard> targetPredicate;

    public EffectTakeIntoHandFromDiscard(Predicate<BotCard> targetPredicate) {
        this.targetPredicate = targetPredicate;
    }

    @Override
    public List<BotCard> getPotentialTargets(BotCard source, PlannedBoardState plannedBoardState) {
        return plannedBoardState.getDiscard(source.getSelf().getOwner()).stream().filter(targetPredicate).toList();
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
            potentialTargets.sort((o1, o2) -> Double.compare(getValueOfTarget(o2, plannedBoardState), getValueOfTarget(o1, plannedBoardState)));
            return potentialTargets.getFirst();
        }
    }

    @Override
    public void resolve(BotCard source, PlannedBoardState plannedBoardState) {
        BotCard target = chooseTarget(source, plannedBoardState);
        if (target == null) return;
        plannedBoardState.moveFromDiscardIntoHand(target);
    }

    @Override
    public double getValueIfResolved(BotCard source, PlannedBoardState plannedBoardState) {
        BotCard target = chooseTarget(source, plannedBoardState);
        return getValueOfTarget(target, plannedBoardState);
    }

    public double getValueOfTarget(BotCard target, PlannedBoardState plannedBoardState) {
        if (target == null) {
            return 0;
        }
        if (target instanceof BotEventCard eventCard) {
            EventAbility ability;
            try {
                ability = target.getEventAbility();
            } catch (IllegalStateException e) {
                // do not know what the card does, assume it's ok to return
                return 1;
            }
            if (!eventCard.canBePlayedNoMatterThePhase(plannedBoardState)) {
                return 0;
            } else {
                return ability.getValueIfUsed(target, plannedBoardState);
            }
        } else {
            throw new IllegalStateException("Cannot compute value of non-event cards");
        }
    }
}
