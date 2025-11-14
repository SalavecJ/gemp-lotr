package com.gempukku.lotro.bots.forge.cards.ability2.effect;

import com.gempukku.lotro.bots.forge.plan.PlannedBoardState;

public class EffectMillOpponent extends Effect {
    private final int amount;

    public EffectMillOpponent(int amount) {
        this.amount = amount;
    }

    @Override
    public void resolve(String player, PlannedBoardState plannedBoardState) {
        String opponent = plannedBoardState.getOpponent(player);
        for (int i = 0; i < amount; i++) {
            plannedBoardState.millCard(opponent);
        }
    }

    @Override
    public double getValueIfResolved(String player, PlannedBoardState plannedBoardState) {
        return amount * 0.2;
    }

    @Override
    public String toString(String player, PlannedBoardState plannedBoardState) {
        if (amount == 1) {
            return "mill a card from opponent's deck";
        }
        return "mill " + amount + " cards from opponent's deck";
    }
}
