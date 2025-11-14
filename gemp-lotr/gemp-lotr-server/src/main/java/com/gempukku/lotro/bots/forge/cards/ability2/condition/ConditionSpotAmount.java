package com.gempukku.lotro.bots.forge.cards.ability2.condition;

import com.gempukku.lotro.bots.forge.cards.abstractcard.BotCard;
import com.gempukku.lotro.bots.forge.plan.PlannedBoardState;

import java.util.function.Predicate;

public class ConditionSpotAmount extends Condition {
    protected final Predicate<BotCard> targetPredicate;
    private final int amount;

    public ConditionSpotAmount(Predicate<BotCard> targetPredicate, int amount) {
        this.targetPredicate = targetPredicate;
        this.amount = amount;
    }

    @Override
    public boolean isOk(String player, PlannedBoardState plannedBoardState) {
        return plannedBoardState.getActiveCards().stream().filter(targetPredicate).count() >= amount;
    }
}
