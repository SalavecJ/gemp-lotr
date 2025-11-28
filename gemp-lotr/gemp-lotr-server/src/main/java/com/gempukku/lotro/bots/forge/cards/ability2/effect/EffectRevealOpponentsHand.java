package com.gempukku.lotro.bots.forge.cards.ability2.effect;

import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.stream.Collectors;

public class EffectRevealOpponentsHand extends Effect{

    public EffectRevealOpponentsHand() {

    }

    @Override
    public double getValueIfResolved(String player, DefaultLotroGame game) {
        return 0.6;
    }

    @Override
    public String toString(String player, DefaultLotroGame game) {
        String cardsInOpponentsHand = game.getGameState().getHand(game.getGameState().getOpponent(player)).stream()
                .map(t -> t.getBlueprint().getFullName())
                .collect(Collectors.joining("; "));
        return "reveal cards from opponent's hand: " + cardsInOpponentsHand;
    }
}
