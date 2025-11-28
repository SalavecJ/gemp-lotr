package com.gempukku.lotro.bots.forge.utils;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;
import com.gempukku.lotro.game.PhysicalCard;

import java.util.ArrayList;
import java.util.List;

public class BoardStateUtil {
    private BoardStateUtil() {

    }

    public static List<PhysicalCard> getCompanionsInPlay(DefaultLotroGame game) {
        return new ArrayList<>(game.getGameState().getActiveCards().stream()
                .filter(card -> CardType.COMPANION.equals(card.getBlueprint().getCardType()))
                .toList());
    }

    public static List<PhysicalCard> getMinionsInPlay(DefaultLotroGame game) {
        return new ArrayList<>(game.getGameState().getActiveCards().stream()
                .filter(card -> card.getBlueprint().getCardType().equals(CardType.MINION))
                .toList());
    }

    /**
     * Gets shadow cards in play that will persist beyond the current turn.
     * This excludes minions (which are discarded at end of turn) and any cards attached to minions.
     * Includes conditions, possessions attached to non-minions, etc.
     */
    public static List<PhysicalCard> getPersistentShadowCards(DefaultLotroGame game) {
        List<PhysicalCard> shadowCards = game.getGameState().getActiveCards().stream()
                .filter(card -> card.getBlueprint().getSide() == Side.SHADOW)
                .toList();

        // Get all minions
        List<PhysicalCard> minions = shadowCards.stream()
                .filter(card -> CardType.MINION.equals(card.getBlueprint().getCardType()))
                .toList();

        // Get non-minion shadow cards that are NOT attached to minions
        return shadowCards.stream()
                .filter(card -> !CardType.MINION.equals(card.getBlueprint().getCardType()))
                .filter(card -> {
                    // Check if this card is attached to a minion
                    for (PhysicalCard minion : minions) {
                        if (game.getGameState().getAttachedCards(minion).contains(card)) {
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
    public static List<PhysicalCard> getFpNonCompanionCards(DefaultLotroGame game) {
        List<PhysicalCard> fpCards = game.getGameState().getActiveCards().stream()
                .filter(card -> card.getBlueprint().getSide() == Side.FREE_PEOPLE)
                .toList();

        return fpCards.stream()
                .filter(card -> !CardType.COMPANION.equals(card.getBlueprint().getCardType()))
                .toList();
    }

    /**
     * Gets all FP allies in play.
     */
    public static List<PhysicalCard> getAlliesInPlay(DefaultLotroGame game) {
        List<PhysicalCard> fpCards = game.getGameState().getActiveCards().stream()
                .filter(card -> card.getBlueprint().getSide() == Side.FREE_PEOPLE)
                .toList();

        return fpCards.stream()
                .filter(card -> CardType.ALLY.equals(card.getBlueprint().getCardType()))
                .toList();
    }
}
