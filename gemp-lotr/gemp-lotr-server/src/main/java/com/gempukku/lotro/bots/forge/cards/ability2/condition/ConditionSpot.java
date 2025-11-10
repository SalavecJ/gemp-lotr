package com.gempukku.lotro.bots.forge.cards.ability2.condition;

import com.gempukku.lotro.bots.forge.cards.abstractcard.BotCard;
import com.gempukku.lotro.bots.forge.plan.PlannedBoardState;

import java.util.function.Predicate;

public class ConditionSpot extends Condition {
    protected final Predicate<BotCard> targetPredicate;

    public ConditionSpot(Predicate<BotCard> targetPredicate) {
        this.targetPredicate = targetPredicate;
    }

    @Override
    public boolean isOk(String player, PlannedBoardState plannedBoardState) {
        return plannedBoardState.getActiveCards().stream().anyMatch(targetPredicate);
    }
}
