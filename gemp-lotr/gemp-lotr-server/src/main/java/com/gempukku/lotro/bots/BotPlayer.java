package com.gempukku.lotro.bots;

import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

public interface BotPlayer {
    String chooseAction(DefaultLotroGame game, AwaitingDecision awaitingDecision);
    void decisionMadeByPlayer(DefaultLotroGame game, AwaitingDecision awaitingDecision, String answer, String player);
    void cleanUpAfterGame();
    String getName();
}
