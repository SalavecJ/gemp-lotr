package com.gempukku.lotro.bots.forge.cards.ability2.condition;

import com.gempukku.lotro.bots.forge.cards.abstractcard.BotCard;
import com.gempukku.lotro.common.CardType;
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
                .anyMatch(botCard -> {
                    if (botCard.getSelf().getBlueprint().getSide() == Side.FREE_PEOPLE)
                        return true;

                    int twilightCost = botCard.getSelf().getBlueprint().getTwilightCost();
                    if (botCard.getSelf().getBlueprint().getCardType() == CardType.MINION) {
                        int currentSiteNumber = plannedBoardState.getCurrentSite().getSelf().getBlueprint().getSiteNumber();
                        int minionSiteNumber = botCard.getSelf().getBlueprint().getSiteNumber();
                        boolean roaming = minionSiteNumber > currentSiteNumber;
                        if (roaming) {
                            twilightCost += 2;
                        }
                    }
                    return plannedBoardState.getTwilight() >= twilightCost;
                });
    }
}
