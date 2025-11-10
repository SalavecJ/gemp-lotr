package com.gempukku.lotro.bots.forge.cards.ability2.condition;

import com.gempukku.lotro.bots.forge.cards.abstractcard.BotCard;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.bots.forge.plan.PlannedBoardState;

import java.util.function.Predicate;

public class ConditionSpotPlayableInDiscard extends Condition {
    protected final Predicate<BotCard> targetPredicate;

    public ConditionSpotPlayableInDiscard(Predicate<BotCard> targetPredicate) {
        this.targetPredicate = targetPredicate;
    }

    @Override
    public boolean isOk(String player, PlannedBoardState plannedBoardState) {
        return plannedBoardState.getDiscard(player).stream()
                .filter(targetPredicate)
                .filter(botCard -> botCard.canBePlayed(plannedBoardState))
                .anyMatch(botCard -> botCard.getSelf().getBlueprint().getSide() == Side.FREE_PEOPLE ||
                        plannedBoardState.getTwilight() >= botCard.getSelf().getBlueprint().getTwilightCost());
    }
}
