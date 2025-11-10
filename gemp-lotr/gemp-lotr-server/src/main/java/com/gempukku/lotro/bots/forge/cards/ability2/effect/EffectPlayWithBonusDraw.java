package com.gempukku.lotro.bots.forge.cards.ability2.effect;

import com.gempukku.lotro.bots.forge.cards.abstractcard.BotCard;
import com.gempukku.lotro.bots.forge.plan.PlannedBoardState;

import java.util.List;
import java.util.function.Predicate;

public class EffectPlayWithBonusDraw extends EffectPlayWithBonus {

    public EffectPlayWithBonusDraw(Predicate<BotCard> targetPredicate) {
        super(targetPredicate);
    }

    @Override
    public void resolveWithTarget(String player, PlannedBoardState plannedBoardState, BotCard target) {
        plannedBoardState.playCard(target);
        plannedBoardState.drawCard(player);
    }

    @Override
    public double getValueIfResolvedWithTarget(String player, PlannedBoardState plannedBoardState, BotCard target) {
        if (plannedBoardState.ruleOfFourLimitOk()) {
            return 0.6;
        } else {
            return 0.0;
        }
    }

    @Override
    public double getValueIfResolved(String player, PlannedBoardState plannedBoardState) {
        if (plannedBoardState.ruleOfFourLimitOk()) {
            return 0.6;
        } else {
            return 0.0;
        }
    }

    @Override
    public String toString(String player, PlannedBoardState plannedBoardState, List<BotCard> targets) {
        if (targets.isEmpty()) {
            return "attempt to play card from hand with bonus draw, but none can be chosen";
        } else if (targets.size() == 1) {
            return "play " + targets.getFirst().getSelf().getBlueprint().getFullName() + " from hand to draw a card: " + plannedBoardState.getTopCardOfDeck(player).getSelf().getBlueprint().getFullName();
        } else {
            throw new IllegalStateException("EffectPlayWithBonusDraw cannot be applied to multiple targets");
        }
    }
}
