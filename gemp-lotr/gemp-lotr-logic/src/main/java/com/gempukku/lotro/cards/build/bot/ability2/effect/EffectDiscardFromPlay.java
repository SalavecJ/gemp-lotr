package com.gempukku.lotro.cards.build.bot.ability2.effect;

import com.gempukku.lotro.cards.build.bot.abstractcard.BotCard;
import com.gempukku.lotro.game.state.PlannedBoardState;

import java.util.List;
import java.util.function.Predicate;

public class EffectDiscardFromPlay extends EffectWithTarget{
    protected final Predicate<BotCard> targetPredicate;
    protected final boolean discardAll;

    public EffectDiscardFromPlay(Predicate<BotCard> targetPredicate, boolean discardAll) {
        this.targetPredicate = targetPredicate;
        this.discardAll = discardAll;
    }

    @Override
    public List<BotCard> getPotentialTargets(PlannedBoardState plannedBoardState) {
        return plannedBoardState.getActiveCards().stream().filter(targetPredicate).toList();
    }

    @Override
    public boolean affectsAll() {
        return discardAll;
    }

    @Override
    public BotCard chooseTarget(PlannedBoardState plannedBoardState) {
        throw new IllegalStateException("Choosing targets for Discard from Play effect not implemented");
    }

    @Override
    public void resolve(BotCard source, PlannedBoardState plannedBoardState) {
        List<BotCard> potentialTargets = getPotentialTargets(plannedBoardState);
        if (potentialTargets.isEmpty()) {
            return;
        } else if (potentialTargets.size() == 1) {
            plannedBoardState.discardFromPlay(potentialTargets.getFirst());
        } else {
            if (discardAll) {
                potentialTargets.forEach(plannedBoardState::discardFromPlay);
            } else {
                throw new IllegalStateException("Cannot resolve discard effect if number of potential targets is greater than 1 and not all should be discarded");
            }
        }
    }

    @Override
    public double getValueIfResolved(BotCard source, PlannedBoardState plannedBoardState) {
        List<BotCard> potentialTargets = getPotentialTargets(plannedBoardState);
        if (discardAll) {
            double value = 0.0;
            for (BotCard potentialTarget : potentialTargets) {
                // discarding enemy cards good, discarding own cards bad
                value += potentialTarget.getSelf().getOwner().equals(source.getSelf().getOwner()) ? -1.1: 1.1;
            }
            return value;
        } else {
            if (potentialTargets.isEmpty()) {
                return 0;
            } else if (potentialTargets.size() == 1) {
                return potentialTargets.getFirst().getSelf().getOwner().equals(source.getSelf().getOwner()) ? -1.1: 1.1;
            } else {
                throw new IllegalStateException("Cannot resolve discard value if number of potential targets is greater than 1");
            }
        }
    }
}
