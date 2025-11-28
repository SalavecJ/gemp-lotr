package com.gempukku.lotro.bots.forge.cards.ability2.effect;

import com.gempukku.lotro.bots.forge.cards.ability2.Target;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.function.Predicate;

public abstract class Effect {
    public abstract double getValueIfResolved(String player, DefaultLotroGame game);
    public abstract String toString(String player, DefaultLotroGame game);

    public static EffectTakeIntoHandFromDiscard takeIntoHandFromDiscard(Predicate<PhysicalCard> target) {
        return new EffectTakeIntoHandFromDiscard(target);
    }

    public static EffectPutFromHandToBottomOfDeck putFromHandToBottomOfDeck(Predicate<PhysicalCard> target) {
        return new EffectPutFromHandToBottomOfDeck(target);
    }

    public static EffectPutFromHandToBottomOfDeck putFromHandToBottomOfDeck() {
        return putFromHandToBottomOfDeck(PhysicalCard -> true);
    }

    public static EffectPutFromDiscardToBottomOfDeck putFromDiscardToBottomOfDeck(Predicate<PhysicalCard> target) {
        return new EffectPutFromDiscardToBottomOfDeck(target);
    }

    public static EffectMillOpponent millOpponent(int amount) {
        return new EffectMillOpponent(amount);
    }

    public static EffectRemoveBurden removeBurden() {
        return removeBurden(1);
    }

    public static EffectRemoveBurden removeBurden(int amount) {
        return new EffectRemoveBurden(amount);
    }

    public static EffectDiscardFromPlay discardFromPlayAll(Predicate<PhysicalCard> target) {
        return discardFromPlay(target, true);
    }

    public static EffectDiscardFromPlay discardFromPlay(Predicate<PhysicalCard> target) {
        return discardFromPlay(target, false);
    }

    public static EffectDiscardFromPlay discardFromPlay(Predicate<PhysicalCard> target, boolean discardAll) {
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

    public static EffectHeal heal(Predicate<PhysicalCard> target) {
        return heal(target, 1);
    }

    public static EffectHeal heal(Predicate<PhysicalCard> target, int amount) {
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

    public static EffectPlayWithBonusDraw playWithBonusDraw(Predicate<PhysicalCard> target){
        return new EffectPlayWithBonusDraw(target);
    }

    public static EffectPlayWithBonusDraw playWithBonusDraw(Race race){
        return Effect.playWithBonusDraw(Target.race(race));
    }

    public static EffectPlayWithBonusTwilightModification playWithBonusTwilightModification(Predicate<PhysicalCard> target, int twilightModification){
        return new EffectPlayWithBonusTwilightModification(target, twilightModification);
    }

    public static EffectPlayMinionFromDiscard playMinionFromDiscard(Predicate<PhysicalCard> target){
        return new EffectPlayMinionFromDiscard(target);
    }

    public static EffectPlayPossessionFromDiscardOn playPossessionFromDiscardOn(Predicate<PhysicalCard> possessionPredicate, Predicate<PhysicalCard> bearerPredicate){
        return new EffectPlayPossessionFromDiscardOn(possessionPredicate, bearerPredicate);
    }

    public static EffectAddTwilight addTwilight(int amount){
        return new EffectAddTwilight(amount);
    }

    public static EffectDrawCard drawCard(int amount){
        return new EffectDrawCard(amount);
    }

    public static EffectPreventFellowshipFromMoving preventFellowshipFromMoving() {
        return new EffectPreventFellowshipFromMoving();
    }
}
