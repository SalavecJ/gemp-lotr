package com.gempukku.lotro.cards.build.bot.ability2;

import com.gempukku.lotro.cards.build.bot.abstractcard.BotCard;
import com.gempukku.lotro.common.*;

import java.util.function.Predicate;

public class Target {
    public static Predicate<BotCard> and(Predicate<BotCard> a, Predicate<BotCard> b) {
        return card -> a.test(card) && b.test(card);
    }

    public static Predicate<BotCard> or(Predicate<BotCard> a, Predicate<BotCard> b) {
        return card -> a.test(card) || b.test(card);
    }

    public static Predicate<BotCard> not(Predicate<BotCard> a) {
        return card -> !a.test(card);
    }

    public static Predicate<BotCard> self(BotCard self) {
        return card -> card.getSelf().equals(self.getSelf());
    }

    public static Predicate<BotCard> bearer(BotCard self) {
        return card -> card.getSelf().equals(self.getSelf().getAttachedTo());
    }

    public static Predicate<BotCard> any() {
        return card -> true;
    }

    public static Predicate<BotCard> culture(Culture culture) {
        return card -> culture.equals(card.getSelf().getBlueprint().getCulture());
    }

    public static Predicate<BotCard> race(Race race) {
        return card -> race.equals(card.getSelf().getBlueprint().getRace());
    }

    public static Predicate<BotCard> cardType(CardType cardType) {
        return card -> cardType.equals(card.getSelf().getBlueprint().getCardType());
    }

    public static Predicate<BotCard> side(Side side) {
        return card -> side.equals(card.getSelf().getBlueprint().getSide());
    }

    public static Predicate<BotCard> possessionClass(PossessionClass possessionClass) {
        return card -> card.getSelf().getBlueprint().getPossessionClasses() != null && card.getSelf().getBlueprint().getPossessionClasses().contains(possessionClass);
    }

    public static Predicate<BotCard> title(String title) {
        return card -> title.equals(card.getSelf().getBlueprint().getTitle());
    }

    public static Predicate<BotCard> signet(Signet signet) {
        return card -> signet.equals(card.getSelf().getBlueprint().getSignet());
    }

    public static Predicate<BotCard> attachedTo(CardType cardType) {
        return card -> card.getSelf().getAttachedTo() != null && cardType.equals(card.getSelf().getAttachedTo().getBlueprint().getCardType());
    }

    public static Predicate<BotCard> keyword(Keyword keyword) {
        return card -> card.getSelf().getBlueprint().hasKeyword(keyword);
    }
}
