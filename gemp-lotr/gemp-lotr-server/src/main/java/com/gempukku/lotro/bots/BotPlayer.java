package com.gempukku.lotro.bots;

import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

public interface BotPlayer {
    String chooseAction(LotroGame game, AwaitingDecision awaitingDecision);
    String getName();
}
