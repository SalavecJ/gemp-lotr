package com.gempukku.lotro.bots.forge.cards.ability2.condition;

import com.gempukku.lotro.bots.forge.cards.abstractcard.BotCard;
import com.gempukku.lotro.bots.forge.cards.abstractcard.BotObjectAttachableCard;
import com.gempukku.lotro.bots.forge.plan.PlannedBoardState;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Side;

import java.util.function.Predicate;

public class ConditionSpotPossessionPlayableInDiscardOn extends Condition {
    protected final Predicate<BotCard> possessionPredicate;
    protected final Predicate<BotCard> onPredicate;

    public ConditionSpotPossessionPlayableInDiscardOn(Predicate<BotCard> possessionPredicate, Predicate<BotCard> onPredicate) {
        this.possessionPredicate = possessionPredicate;
        this.onPredicate = onPredicate;
    }

    @Override
    public boolean isOk(String player, PlannedBoardState plannedBoardState) {
        return plannedBoardState.getDiscard(player).stream()
                .filter(possessionPredicate)
                .filter(botCard -> botCard.canBePlayed(plannedBoardState))
                .filter(botCard -> botCard instanceof BotObjectAttachableCard)
                .filter(botCard -> plannedBoardState.getActiveCards().stream().anyMatch(activeCard -> {
                    BotObjectAttachableCard attachableCard = (BotObjectAttachableCard) botCard;
                    return attachableCard.isValidBearer(activeCard, plannedBoardState)
                            && possessionPredicate.test(attachableCard);
                }))
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
