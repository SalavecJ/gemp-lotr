package com.gempukku.lotro.cards.build.bot.ability2.condition;

import com.gempukku.lotro.cards.build.bot.abstractcard.BotCard;
import com.gempukku.lotro.game.state.PlannedBoardState;

import java.util.function.Predicate;

public class ConditionSpotInDiscard extends Condition {
    protected final Predicate<BotCard> targetPredicate;

    public ConditionSpotInDiscard(Predicate<BotCard> targetPredicate) {
        this.targetPredicate = targetPredicate;
    }

    @Override
    public boolean isOk(String player, PlannedBoardState plannedBoardState) {
        return plannedBoardState.getDiscard(player).stream().anyMatch(targetPredicate);
    }
}
