package com.gempukku.lotro.bots.v2.fotr_starter.fellowship;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.game.PhysicalCard;

import java.util.function.Predicate;

public final class CardPredicates {
    private CardPredicates() {
    }

    public static Predicate<PhysicalCard> hasFullName(String fullName) {
        return card -> card.getBlueprint().getFullName().equals(fullName);
    }

    public static Predicate<PhysicalCard> hasCardType(CardType cardType) {
        return card -> card.getBlueprint().getCardType() == cardType;
    }
}
