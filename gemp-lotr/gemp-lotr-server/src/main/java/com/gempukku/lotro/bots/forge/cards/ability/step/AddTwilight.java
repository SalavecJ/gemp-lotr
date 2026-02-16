package com.gempukku.lotro.bots.forge.cards.ability.step;

import com.gempukku.lotro.bots.forge.cards.ability.AbilityStep;
import com.gempukku.lotro.bots.forge.cards.ability.targeting.BotTargetingPolicy;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

public class AddTwilight extends AbilityStep {
    private final int twilightAmount;

    public AddTwilight(int twilightAmount) {
        this.twilightAmount = twilightAmount;
    }

    @Override
    public BotTargetingPolicy getBotTargetingPolicy() {
        return null;
    }

    @Override
    public String toString() {
        return "Add " + twilightAmount + " twilight";
    }

    @Override
    public boolean decisionTextMatches(String decisionText) {
        return false;
    }

    @Override
    public double getValue(DefaultLotroGame game, String playerName) {
        if (game.getGameState().getCurrentPlayerId().equals(playerName)) {
            return -twilightAmount * 0.5; // negative value for FP player
        } else {
            return twilightAmount * 0.5; // positive value for Shadow player
        }
    }
}
