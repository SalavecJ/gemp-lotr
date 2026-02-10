package com.gempukku.lotro.bots.forge.plan;

import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

public interface Plan {
    String chooseActionToTakeOrPass(DefaultLotroGame game, AwaitingDecision awaitingDecision);
    void decisionMadeByPlayer(AwaitingDecision awaitingDecision, String answer, String player);
    boolean isOutdated();
}
