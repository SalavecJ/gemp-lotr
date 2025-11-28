package com.gempukku.lotro.bots.forge.cards.ability2.effect;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

public class EffectDrawCard extends Effect {
    protected final int amount ;

    public EffectDrawCard(int amount) {
        this.amount = amount;
    }


    @Override
    public double getValueIfResolved(String player, DefaultLotroGame game) {
        int realAmount = amount;
        if (game.getGameState().getCurrentPhase() == Phase.FELLOWSHIP) {
            int drawnCards = game.getModifiersQuerying().getFellowshipDrawnCards(game);
            int reminder = 4 - drawnCards;
            if (reminder <= 0)
                reminder = 0;
            realAmount = Math.min(amount, reminder);
        }

        return 0.6 * realAmount;
    }

    @Override
    public String toString(String player, DefaultLotroGame game) {
        int burdensPlaced = game.getGameState().getBurdens();
        int toBeRemoved = Math.min(amount, burdensPlaced);
        if (toBeRemoved == 1) {
            return "draw a card";
        }
        return "draw " + toBeRemoved + " cards";
    }
}
