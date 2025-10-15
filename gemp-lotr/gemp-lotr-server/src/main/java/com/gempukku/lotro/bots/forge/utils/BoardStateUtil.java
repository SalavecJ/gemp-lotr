package com.gempukku.lotro.bots.forge.utils;

import com.gempukku.lotro.cards.build.bot.abstractcard.BotCard;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.game.state.PlannedBoardState;

import java.util.List;

public class BoardStateUtil {
    private BoardStateUtil() {

    }

    public static List<BotCard> getWoundedActiveCompanionsInPlay(PlannedBoardState plannedBoardState) {
        return plannedBoardState.getActiveCards().stream()
                .filter(botCard ->
                        CardType.COMPANION.equals(botCard.getSelf().getBlueprint().getCardType())
                        && botCard.getSelf().getBlueprint().isUnique()
                        && plannedBoardState.getWounds(botCard) > 0).toList();
    }

    public static int getActiveCompanionsInPlayCount(PlannedBoardState plannedBoardState) {
        return Math.toIntExact(plannedBoardState.getActiveCards().stream()
                .filter(card -> CardType.COMPANION.equals(card.getSelf().getBlueprint().getCardType()))
                .count());
    }

    public static int getCompanionsInDeadPileCount(PlannedBoardState plannedBoardState) {
        return Math.toIntExact(plannedBoardState.getDeadPile(plannedBoardState.getCurrentFpPlayer()).stream()
                .filter(card -> CardType.COMPANION.equals(card.getSelf().getBlueprint().getCardType()))
                .count());
    }

    public static int getRuleOfNineRemainder(PlannedBoardState plannedBoardState) {
        return 9 - getActiveCompanionsInPlayCount(plannedBoardState) - getCompanionsInDeadPileCount(plannedBoardState);

    }
}
