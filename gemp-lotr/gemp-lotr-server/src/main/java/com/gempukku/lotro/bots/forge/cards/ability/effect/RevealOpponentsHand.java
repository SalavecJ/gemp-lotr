package com.gempukku.lotro.bots.forge.cards.ability.effect;

import com.gempukku.lotro.bots.forge.cards.ability.targeting.BotTargetingPolicy;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

public class RevealOpponentsHand extends Effect {
    @Override
    public BotTargetingPolicy getBotTargetingPolicy() {
        return null;
    }

    @Override
    public String toString() {
        return "Reveal an opponent's hand";
    }

    @Override
    public boolean decisionTextMatches(String decisionText) {
        return false;
    }

    @Override
    public double getValue(DefaultLotroGame game, String playerName) {
        return 1.0;
    }
}
