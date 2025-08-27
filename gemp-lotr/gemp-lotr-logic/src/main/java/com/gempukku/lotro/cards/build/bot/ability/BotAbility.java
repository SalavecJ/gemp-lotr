package com.gempukku.lotro.cards.build.bot.ability;

import com.gempukku.lotro.cards.build.bot.BotTargetingMode;
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
