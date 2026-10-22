package com.gempukku.lotro.bots.v2.decision.request;

import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.*;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

public class DecisionRequestFactory {

    public static DecisionRequest create(DefaultLotroGame game, AwaitingDecision awaitingDecision) {
        if (awaitingDecision instanceof IntegerAwaitingDecision integerAwaitingDecision) {
            return new IntegerDecisionRequest(integerAwaitingDecision, game);
        } else if (awaitingDecision instanceof MultipleChoiceAwaitingDecision multipleChoiceAwaitingDecision) {
            return new MultipleChoiceDecisionRequest(multipleChoiceAwaitingDecision, game);
        } else if (awaitingDecision instanceof ArbitraryCardsSelectionDecision arbitraryCardsSelectionDecision) {
            try {
                return new ArbitraryCardsSelectionDecisionRequest(arbitraryCardsSelectionDecision, game);
            } catch (CardNotFoundException e) {
                throw new IllegalArgumentException("Decision " + awaitingDecision.toJson().toString() + " contains unknown card: " + e.getMessage());
            }
        } else if (awaitingDecision instanceof CardActionSelectionDecision cardActionSelectionDecision) {
            return new CardActionSelectionDecisionRequest(cardActionSelectionDecision, game);
        } else if (awaitingDecision instanceof CardsSelectionDecision cardsSelectionDecision) {
            return new CardsSelectionDecisionRequest(cardsSelectionDecision, game);
        } else if (awaitingDecision instanceof PlayerAssignMinionsDecision assignMinionsDecision) {
            return new AssignMinionsDecisionRequest(assignMinionsDecision, game);
        } else if (awaitingDecision instanceof ActionSelectionDecision actionSelectionDecision) {
            return new ActionChoiceDecisionRequest(actionSelectionDecision, game);
        }
        throw new UnsupportedOperationException("Unknown decision: " + awaitingDecision.toJson().toString());
    }
}
