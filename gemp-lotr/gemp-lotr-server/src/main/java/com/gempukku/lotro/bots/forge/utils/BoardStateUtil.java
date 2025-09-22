package com.gempukku.lotro.bots.forge.utils;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

import java.util.List;
import java.util.function.Predicate;

public class BoardStateUtil {
    private BoardStateUtil() {

    }

    public static List<PhysicalCard> getWoundedCompanionsInPlay(LotroGame game, String owner) {
        return (List<PhysicalCard>) game.getGameState().getInPlay().stream()
                .filter((Predicate<PhysicalCard>) card -> card.getOwner().equals(owner))
                .filter((Predicate<PhysicalCard>) card ->
                        CardType.COMPANION.equals(card.getBlueprint().getCardType())
                                && card.getBlueprint().isUnique()
                                && game.getGameState().getWounds(card) > 0
                                && game.getModifiersQuerying().canBeHealed(game, card))
                .toList();
    }

    public static int getCompanionsInPlayCount(LotroGame game, String owner) {
        return Math.toIntExact(game.getGameState().getInPlay().stream()
                .filter((Predicate<PhysicalCard>) card -> card.getOwner().equals(owner))
                .filter((Predicate<PhysicalCard>) card -> CardType.COMPANION.equals(card.getBlueprint().getCardType()))
                .count());
    }

    public static int getCompanionsInDeadPileCount(LotroGame game, String owner) {
        return Math.toIntExact(game.getGameState().getDeadPile(owner).stream()
                .filter((Predicate<PhysicalCard>) card -> CardType.COMPANION.equals(card.getBlueprint().getCardType()))
                .count());
    }

    public static int getRuleOfNineRemainder(LotroGame game, String player) {
        return 9 - getCompanionsInPlayCount(game, player) - getCompanionsInDeadPileCount(game, player);

    }
}
