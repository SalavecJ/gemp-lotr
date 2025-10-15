package com.gempukku.lotro.cards.build.bot.ability2.condition;

import com.gempukku.lotro.cards.build.bot.abstractcard.BotCard;
import com.gempukku.lotro.game.state.PlannedBoardState;

import java.util.function.Predicate;

public class ConditionSpotInDiscard extends Condition {
    protected final Predicate<BotCard> targetPredicate;
    protected final String player;

    public ConditionSpotInDiscard(Predicate<BotCard> targetPredicate, String player) {
        this.targetPredicate = targetPredicate;
        this.player = player;
    }

    @Override
    public boolean isOk(BotCard source, PlannedBoardState plannedBoardState) {
        return plannedBoardState.getDiscard(player).stream().anyMatch(targetPredicate);
    }
}
