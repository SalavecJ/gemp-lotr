package com.gempukku.lotro.cards.build.bot.ability2.effect;

import com.gempukku.lotro.cards.build.bot.ability2.Target;
import com.gempukku.lotro.cards.build.bot.abstractcard.BotCard;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.game.state.PlannedBoardState;

import java.util.function.Predicate;

public abstract class Effect {
    public abstract void resolve(BotCard source, PlannedBoardState plannedBoardState);
    public abstract double getValueIfResolved(BotCard source, PlannedBoardState plannedBoardState);


    public static EffectTakeIntoHandFromDiscard takeIntoHandFromDiscard(Predicate<BotCard> target) {
        return new EffectTakeIntoHandFromDiscard(target);
    }

    public static EffectPutFromHandToBottomOfDeck putFromHandToBottomOfDeck(Predicate<BotCard> target) {
        return new EffectPutFromHandToBottomOfDeck(target);
    }

    public static EffectPutFromHandToBottomOfDeck putFromHandToBottomOfDeck() {
        return putFromHandToBottomOfDeck(botCard -> true);
    }

    public static EffectRemoveBurden removeBurden() {
        return removeBurden(1);
    }

    public static EffectRemoveBurden removeBurden(int amount) {
        return new EffectRemoveBurden(amount);
    }

    public static EffectDiscardFromPlay discardFromPlayAll(Predicate<BotCard> target) {
        return discardFromPlay(target, true);
    }

    public static EffectDiscardFromPlay discardFromPlay(Predicate<BotCard> target) {
        return discardFromPlay(target, false);
    }

    public static EffectDiscardFromPlay discardFromPlay(Predicate<BotCard> target, boolean discardAll) {
        return new EffectDiscardFromPlay(target, discardAll);
    }

    public static EffectDiscardFromPlay discardFromPlayAll(CardType cardType) {
        return discardFromPlay(cardType, true);
    }

    public static EffectDiscardFromPlay discardFromPlay(CardType cardType) {
        return discardFromPlay(cardType, false);
    }

    public static EffectDiscardFromPlay discardFromPlay(CardType cardType, boolean discardAll) {
        return discardFromPlay(Target.cardType(cardType), discardAll);
    }

    public static EffectRevealOpponentsHand revealOpponentsHand() {
        return new EffectRevealOpponentsHand();
    }

    public static EffectPlayFellowshipsNextSite playFellowshipsNextSite() {
        return new EffectPlayFellowshipsNextSite();
    }

    public static EffectHeal heal(Predicate<BotCard> target) {
        return heal(target, 1);
    }

    public static EffectHeal heal(Predicate<BotCard> target, int amount) {
        return new EffectHeal(target, amount);
    }

    public static EffectHeal heal(String title) {
        return heal(title, 1);
    }

    public static EffectHeal heal(String title, int amount) {
        return heal(Target.title(title), amount);
    }

    public static EffectHeal heal(CardType cardType) {
        return heal(cardType, 1);
    }

    public static EffectHeal heal(CardType cardType, int amount) {
        return heal(Target.cardType(cardType), amount);
    }
}
