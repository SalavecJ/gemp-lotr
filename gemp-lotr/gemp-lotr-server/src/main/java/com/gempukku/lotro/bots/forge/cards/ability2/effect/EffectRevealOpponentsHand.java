package com.gempukku.lotro.bots.forge.cards.ability2.effect;

import com.gempukku.lotro.bots.forge.plan.PlannedBoardState;

import java.util.stream.Collectors;

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

    @Override
    public String toString(String player, PlannedBoardState plannedBoardState) {
        String cardsInOpponentsHand = plannedBoardState.getHand(plannedBoardState.getOpponent(player)).stream()
                .map(t -> t.getSelf().getBlueprint().getFullName())
                .collect(Collectors.joining("; "));
        return "reveal cards from opponent's hand: " + cardsInOpponentsHand;
    }
}
