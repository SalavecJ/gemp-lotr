package com.gempukku.lotro.cards.build.bot.ability2.effect;

import com.gempukku.lotro.game.state.PlannedBoardState;

public class EffectRevealOpponentsHand extends Effect{

    public EffectRevealOpponentsHand() {

    }

    @Override
    public void resolve(String player, PlannedBoardState plannedBoardState) {
        String opponent =  plannedBoardState.getOpponent(player);
        plannedBoardState.revealHand(opponent);
    }

    @Override
    public double getValueIfResolved(String player, PlannedBoardState plannedBoardState) {
        String opponent =  plannedBoardState.getOpponent(player);
        if (plannedBoardState.allCardsInHandRevealed(opponent)) {
            return 0.0;
        }

        return 0.6;
    }
}
