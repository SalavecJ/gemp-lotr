package com.gempukku.lotro.bots.forge.cards.ability.step;

import com.gempukku.lotro.bots.forge.cards.ability.AbilityStep;
import com.gempukku.lotro.bots.forge.cards.ability.targeting.BotTargetingPolicy;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

public class DrawCard extends AbilityStep {
    private final int amount;

    public DrawCard(int amount) {
        this.amount = amount;
    }

    public DrawCard() {
        this(1);
    }

    @Override
    public BotTargetingPolicy getBotTargetingPolicy() {
        return null;
    }

    @Override
    public String toString() {
        if (amount == 1) {
            return "Draw a card";
        } else {
            return "Draw " + amount + " cards";
        }
    }

    @Override
    public boolean decisionTextMatches(String decisionText) {
        return false;
    }

    @Override
    public double getValue(DefaultLotroGame game, String playerName) {
        return amount * 0.6;
    }
}
