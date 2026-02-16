package com.gempukku.lotro.bots.forge.cards.ability;

import com.gempukku.lotro.bots.forge.cards.ability.targeting.BotTargetingPolicy;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

public abstract class AbilityStep {

    public abstract BotTargetingPolicy getBotTargetingPolicy();

    public abstract String toString();

    public abstract boolean decisionTextMatches(String decisionText);

    public abstract double getValue(DefaultLotroGame game, String playerName);
}
