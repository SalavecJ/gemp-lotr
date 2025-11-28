package com.gempukku.lotro.bots;

import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

public interface BotPlayer {
    String chooseAction(DefaultLotroGame game, AwaitingDecision awaitingDecision);
    void cleanUpAfterGame();
    String getName();
}
