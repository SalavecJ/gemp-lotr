package com.gempukku.lotro.cards.build.bot.ability2.effect;

import com.gempukku.lotro.cards.build.bot.abstractcard.BotCard;
import com.gempukku.lotro.game.state.PlannedBoardState;

public class EffectRevealOpponentsHand extends Effect{

    public EffectRevealOpponentsHand() {

    }

    @Override
    public void resolve(BotCard source, PlannedBoardState plannedBoardState) {
        String opponent =  plannedBoardState.getOpponent(source.getSelf().getOwner());
        plannedBoardState.revealHand(opponent);
    }

    @Override
    public double getValueIfResolved(BotCard source, PlannedBoardState plannedBoardState) {
        String opponent =  plannedBoardState.getOpponent(source.getSelf().getOwner());
        if (plannedBoardState.allCardsInHandRevealed(opponent)) {
            return 0.0;
        }

        return 0.3;
    }
}
