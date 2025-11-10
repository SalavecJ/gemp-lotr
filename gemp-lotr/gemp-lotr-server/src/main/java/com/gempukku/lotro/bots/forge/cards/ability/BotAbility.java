package com.gempukku.lotro.bots.forge.cards.ability;

import com.gempukku.lotro.bots.forge.cards.BotTargetingMode;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

public interface BotAbility {
    enum AbilityType {
        TRIGGERED, ACTIVATED, CONTINUOUS
    }

    AbilityType getType();
    AbilityProperty getEffect();
    boolean canProduceDecision(LotroGame game, AwaitingDecision awaitingDecision);
    BotTargetingMode getTargetingModeForDecision(LotroGame game, AwaitingDecision awaitingDecision);
}
