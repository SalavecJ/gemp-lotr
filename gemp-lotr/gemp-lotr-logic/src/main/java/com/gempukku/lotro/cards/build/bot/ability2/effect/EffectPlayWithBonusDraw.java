package com.gempukku.lotro.cards.build.bot.ability2.effect;

import com.gempukku.lotro.cards.build.bot.abstractcard.BotAllyCard;
import com.gempukku.lotro.cards.build.bot.abstractcard.BotCard;
import com.gempukku.lotro.cards.build.bot.abstractcard.BotCompanionCard;
import com.gempukku.lotro.game.state.PlannedBoardState;

import java.util.List;
import java.util.function.Predicate;

public class EffectPlayWithBonusDraw extends EffectPlayWithBonus {

    public EffectPlayWithBonusDraw(Predicate<BotCard> targetPredicate) {
        super(targetPredicate);
    }

    @Override
    public void resolveWithTarget(String player, PlannedBoardState plannedBoardState, BotCard target) {
        if (target instanceof BotCompanionCard companionCard) {
            plannedBoardState.playCompanion(companionCard);
        } else if (target instanceof BotAllyCard allyCard) {
            plannedBoardState.playToFpSupportArea(allyCard);
        } else {
            throw new IllegalStateException("Unsupported card type for EffectPlayWithBonusDraw: " + target.getSelf().getBlueprint().getFullName());
        }

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
