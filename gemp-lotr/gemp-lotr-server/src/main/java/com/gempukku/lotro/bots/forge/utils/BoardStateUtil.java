package com.gempukku.lotro.bots.forge.utils;

import com.gempukku.lotro.bots.forge.cards.ability2.EventAbility;
import com.gempukku.lotro.bots.forge.cards.ability2.effect.Effect;
import com.gempukku.lotro.bots.forge.cards.abstractcard.BotCard;
import com.gempukku.lotro.bots.forge.cards.abstractcard.BotEventCard;
import com.gempukku.lotro.common.AllyHome;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.bots.forge.plan.PlannedBoardState;

import java.util.ArrayList;
import java.util.List;

public class BoardStateUtil {
    private BoardStateUtil() {

    }

    public static List<BotCard> getWoundedActiveUniqueCompanionsInPlay(PlannedBoardState plannedBoardState) {
        return plannedBoardState.getActiveCards().stream()
                .filter(botCard ->
                        CardType.COMPANION.equals(botCard.getSelf().getBlueprint().getCardType())
                        && botCard.getSelf().getBlueprint().isUnique()
                        && plannedBoardState.getWounds(botCard) > 0).toList();
    }

    public static List<BotCard> getCompanionsAndAlliesAtHome(PlannedBoardState plannedBoardState) {
        BotCard currentSite = plannedBoardState.getCurrentSite();
        return new ArrayList<>(plannedBoardState.getFpCardsInPlay(plannedBoardState.getCurrentFpPlayer()).stream()
                .filter(botCard -> CardType.COMPANION.equals(botCard.getSelf().getBlueprint().getCardType())
                        || (CardType.ALLY.equals(botCard.getSelf().getBlueprint().getCardType())
                        && botCard.getSelf().getBlueprint().hasAllyHome(new AllyHome(currentSite.getSelf().getBlueprint().getSiteBlock(), currentSite.getSelf().getBlueprint().getSiteNumber()))))
                .toList());
    }

    public static List<BotCard> getCompanionsInPlay(PlannedBoardState plannedBoardState) {
        return new ArrayList<>(plannedBoardState.getFpCardsInPlay(plannedBoardState.getCurrentFpPlayer()).stream()
                .filter(botCard -> CardType.COMPANION.equals(botCard.getSelf().getBlueprint().getCardType()))
                .toList());
    }

    public static List<BotCard> getMinionsInPlay(PlannedBoardState plannedBoardState) {
        return new ArrayList<>(plannedBoardState.getShadowCardsInPlay(plannedBoardState.getCurrentShadowPlayer()).stream()
                .filter(botCard -> botCard.getSelf().getBlueprint().getCardType().equals(CardType.MINION))
                .toList());
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

    /**
     * Gets shadow cards in play that will persist beyond the current turn.
     * This excludes minions (which are discarded at end of turn) and any cards attached to minions.
     * Includes conditions, possessions attached to non-minions, etc.
     */
    public static List<BotCard> getPersistentShadowCards(PlannedBoardState plannedBoardState) {
        List<BotCard> shadowCards = plannedBoardState.getShadowCardsInPlay(plannedBoardState.getCurrentShadowPlayer());

        // Get all minions
        List<BotCard> minions = shadowCards.stream()
                .filter(card -> CardType.MINION.equals(card.getSelf().getBlueprint().getCardType()))
                .toList();

        // Get non-minion shadow cards that are NOT attached to minions
        return shadowCards.stream()
                .filter(card -> !CardType.MINION.equals(card.getSelf().getBlueprint().getCardType()))
                .filter(card -> {
                    // Check if this card is attached to a minion
                    for (BotCard minion : minions) {
                        if (plannedBoardState.getAttachedCards(minion).contains(card)) {
                            return false; // Attached to a minion, will be discarded
                        }
                    }
                    return true; // Not attached to a minion, will persist
                })
                .toList();
    }

    /**
     * Gets FP cards in play excluding companions.
     * Includes possessions, allies, conditions - all persistent cards that support the fellowship.
     * Companions are excluded as they're evaluated separately in damage calculations.
     */
    public static List<BotCard> getFpNonCompanionCards(PlannedBoardState plannedBoardState) {
        List<BotCard> fpCards = plannedBoardState.getFpCardsInPlay(plannedBoardState.getCurrentFpPlayer());

        return fpCards.stream()
                .filter(card -> !CardType.COMPANION.equals(card.getSelf().getBlueprint().getCardType()))
                .toList();
    }

    /**
     * Gets all FP allies in play.
     */
    public static List<BotCard> getAlliesInPlay(PlannedBoardState plannedBoardState) {
        List<BotCard> fpCards = plannedBoardState.getFpCardsInPlay(plannedBoardState.getCurrentFpPlayer());

        return fpCards.stream()
                .filter(card -> CardType.ALLY.equals(card.getSelf().getBlueprint().getCardType()))
                .toList();
    }
}
