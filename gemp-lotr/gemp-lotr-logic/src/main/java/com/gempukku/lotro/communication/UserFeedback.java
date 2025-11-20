package com.gempukku.lotro.communication;

import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

public interface UserFeedback {
    void setGame(LotroGame game);

    void participantDecided(String playerId, String answer);

    AwaitingDecision getAwaitingDecision(String playerId);

    void sendAwaitingDecision(String playerId, AwaitingDecision awaitingDecision);

    boolean hasPendingDecisions();
}
