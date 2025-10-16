package com.gempukku.lotro.bots.forge.utils;

import com.gempukku.lotro.cards.build.bot.ability2.EventAbility;
import com.gempukku.lotro.cards.build.bot.ability2.effect.Effect;
import com.gempukku.lotro.cards.build.bot.abstractcard.BotCard;
import com.gempukku.lotro.cards.build.bot.abstractcard.BotEventCard;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.state.PlannedBoardState;

import java.util.ArrayList;
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

    public static List<BotCard> getCardInHandPlayableInPhase(PlannedBoardState plannedBoardState, String player, Phase phase) {
        return new ArrayList<>(
                plannedBoardState.getHand(player).stream()
                        .filter(card -> card.isPlayableInPhase(phase))
                        .toList());
    }

    public static List<BotEventCard> getPlayableFellowshipEventsWithEffect(PlannedBoardState plannedBoardState, String playerName, Class<? extends Effect> effectClass) {
        return new ArrayList<>(BoardStateUtil.getCardInHandPlayableInPhase(plannedBoardState, playerName, Phase.FELLOWSHIP).stream()
                .filter(botCard -> botCard.getSelf().getBlueprint().getCardType().equals(CardType.EVENT)
                        && botCard.canBePlayed(plannedBoardState))
                .filter(botCard -> {
                    if (botCard instanceof BotEventCard eventCard) {
                        EventAbility eventAbility = eventCard.getEventAbility();
                        return effectClass.isInstance(eventAbility.getEffect());
                    } else {
                        throw new IllegalStateException("Event " + botCard.getSelf().getBlueprint().getFullName() + " is not implemented as BotEventCard");
                    }
                })
                .map(botCard -> (BotEventCard) botCard)
                .toList());
    }
}
