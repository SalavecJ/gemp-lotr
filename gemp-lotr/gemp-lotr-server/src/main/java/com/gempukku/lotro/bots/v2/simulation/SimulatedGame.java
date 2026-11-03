package com.gempukku.lotro.bots.v2.simulation;

import com.gempukku.lotro.bots.v2.decision.request.DecisionRequest;
import com.gempukku.lotro.bots.v2.decision.request.DecisionRequestFactory;
import com.gempukku.lotro.game.DefaultUserFeedback;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

public class SimulatedGame {

    private final DefaultLotroGame copy;
    private final DefaultUserFeedback userFeedback = new DefaultUserFeedback();

    public SimulatedGame(DefaultLotroGame game) {
        this.copy = game.getCopyByReplayingDecisionsFromStart(userFeedback);
    }

    public DecisionRequest getDecisionRequest(String player) {
        AwaitingDecision awaitingDecision = userFeedback.getAwaitingDecision(player);
        if (awaitingDecision == null) {
            return null;
        }
        return DecisionRequestFactory.create(copy, awaitingDecision);
    }

    public void makeDecision(String player, String answer) {
        AwaitingDecision awaitingDecision = userFeedback.getAwaitingDecision(player);
        userFeedback.participantDecided(player, answer);
        try {
            awaitingDecision.decisionMade(answer);
        } catch (DecisionResultInvalidException e) {
            throw new IllegalStateException("Chosen action was invalid: " + answer, e);
        }
        copy.carryOutPendingActionsUntilDecisionNeeded();
    }

    public DefaultLotroGame getGame() {
        return copy;
    }
}
