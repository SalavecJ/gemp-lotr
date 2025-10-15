package com.gempukku.lotro.cards.build.bot.ability2.effect;

import com.gempukku.lotro.cards.build.bot.ability2.Target;
import com.gempukku.lotro.cards.build.bot.abstractcard.BotCard;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.game.state.PlannedBoardState;

public abstract class Effect {
    public abstract void resolve(BotCard source, PlannedBoardState plannedBoardState);
    public abstract double getValueIfResolved(BotCard source, PlannedBoardState plannedBoardState);

    public static EffectRemoveBurden removeBurden() {
        return removeBurden(1);
    }

    public static EffectRemoveBurden removeBurden(int amount) {
        return new EffectRemoveBurden(amount);
    }

    public static EffectDiscardFromPlay discardFromPlayAll(CardType cardType) {
        return discardFromPlay(cardType, true);
    }

    public static EffectDiscardFromPlay discardFromPlay(CardType cardType, boolean discardAll) {
        return new EffectDiscardFromPlay(Target.cardType(cardType), discardAll);
    }

    public static EffectRevealOpponentsHand revealOpponentsHand() {
        return new EffectRevealOpponentsHand();
    }

    public static EffectPlayFellowshipsNextSite playFellowshipsNextSite() {
        return new EffectPlayFellowshipsNextSite();
    }
}
