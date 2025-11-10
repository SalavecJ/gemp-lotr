package com.gempukku.lotro.bots.forge.cards.ability;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.PhysicalCard;

import java.util.function.Predicate;

public class Target {
    public static Predicate<PhysicalCard> and(Predicate<PhysicalCard> a, Predicate<PhysicalCard> b) {
        return card -> a.test(card) && b.test(card);
    }

    public static Predicate<PhysicalCard> or(Predicate<PhysicalCard> a, Predicate<PhysicalCard> b) {
        return card -> a.test(card) || b.test(card);
    }

    public static Predicate<PhysicalCard> not(Predicate<PhysicalCard> a) {
        return card -> !a.test(card);
    }

    public static Predicate<PhysicalCard> self(PhysicalCard self) {
        return card -> card.equals(self);
    }

    public static Predicate<PhysicalCard> bearer(PhysicalCard self) {
        return card -> card.equals(self.getAttachedTo());
    }

    public static Predicate<PhysicalCard> any() {
        return card -> true;
    }

    public static Predicate<PhysicalCard> culture(Culture culture) {
        return card -> culture.equals(card.getBlueprint().getCulture());
    }

    public static Predicate<PhysicalCard> race(Race race) {
        return card -> race.equals(card.getBlueprint().getRace());
    }

    public static Predicate<PhysicalCard> cardType(CardType cardType) {
        return card -> cardType.equals(card.getBlueprint().getCardType());
    }

    public static Predicate<PhysicalCard> side(Side side) {
        return card -> side.equals(card.getBlueprint().getSide());
    }

    public static Predicate<PhysicalCard> possessionClass(PossessionClass possessionClass) {
        return card -> card.getBlueprint().getPossessionClasses() != null && card.getBlueprint().getPossessionClasses().contains(possessionClass);
    }

    public static Predicate<PhysicalCard> title(String title) {
        return card -> title.equals(card.getBlueprint().getTitle());
    }

    public static Predicate<PhysicalCard> signet(Signet signet) {
        return card -> card.getGame() != null ? card.getGame().getModifiersQuerying().hasSignet(card.getGame(), card, signet) : signet.equals(card.getBlueprint().getSignet());
    }

    public static Predicate<PhysicalCard> attachedTo(CardType cardType) {
        return card -> card.getAttachedTo() != null && cardType.equals(card.getAttachedTo().getBlueprint().getCardType());
    }

    public static Predicate<PhysicalCard> keyword(Keyword keyword) {
        return card -> card.getGame() != null ? card.getGame().getModifiersQuerying().hasKeyword(card.getGame(), card, keyword) : card.getBlueprint().hasKeyword(keyword);
    }
}
