package com.gempukku.lotro.cards.build.bot.ability2.effect;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.state.PlannedBoardState;

public class EffectDrawCard extends Effect{
    protected final int amount ;

    public EffectDrawCard(int amount) {
        this.amount = amount;
    }

    @Override
    public void resolve(String player, PlannedBoardState plannedBoardState) {
        for (int i = 0; i < amount; i++) {
            plannedBoardState.drawCard(player);
        }
    }

    @Override
    public double getValueIfResolved(String player, PlannedBoardState plannedBoardState) {
        int realAmount = amount;
        if (plannedBoardState.getCurrentPhase() == Phase.FELLOWSHIP) {
            realAmount = Math.min(amount, plannedBoardState.getRuleOfFourReminder());
        }

        return 0.6 * realAmount;
    }

    @Override
    public String toString(String player, PlannedBoardState plannedBoardState) {
        int burdensPlaced = plannedBoardState.getBurdens();
        int toBeRemoved = Math.min(amount, burdensPlaced);
        if (toBeRemoved == 1) {
            return "draw a card";
        }
        return "draw " + toBeRemoved + " cards";
    }
}
