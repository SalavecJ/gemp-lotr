package com.gempukku.lotro.bots.rl.v2.decisions.arbitrary.specific;

import com.gempukku.lotro.bots.rl.v2.decisions.AnswerersV2;
import com.gempukku.lotro.bots.rl.v2.decisions.arbitrary.AbstractArbitraryAnswerer;
import com.gempukku.lotro.bots.rl.v2.learning.TrainersV2;
import com.gempukku.lotro.bots.rl.v2.learning.arbitrary.AbstractArbitraryTrainer;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

import java.util.HashMap;
import java.util.Map;

public class SpecificArbitraryCardSelectionFactory {
    private SpecificArbitraryCardSelectionFactory() {

    }

    private static Map<AbstractArbitraryTrainer, AbstractArbitraryAnswerer> makeTrainerAnswererPair(AwaitingDecision decision) {
        if (!(decision instanceof ArbitraryCardsSelectionDecision)) {
            throw new IllegalArgumentException("Decision provided is not " + ArbitraryCardsSelectionDecision.class.getSimpleName());
        }

        AbstractArbitraryTrainer trainer = new AbstractArbitraryTrainer() {
            private final String source = decision.getDecisionParameters().get("source")[0];

            @Override
            protected String getTextTrigger() {
                return decision.getText();
            }

            @Override
            public String getName() {
                return "AcsTrainer-" + source + "-" + decision.getText();
            }

            @Override
            public boolean isDecisionRelevant(GameState gameState, AwaitingDecision decision, String playerName) {
                return super.isDecisionRelevant(gameState, decision, playerName)
                        && decision.getDecisionParameters().get("source")[0].equals(source);
            }
        };

        AbstractArbitraryAnswerer answerer = new AbstractArbitraryAnswerer() {
            private final String source = decision.getDecisionParameters().get("source")[0];

            @Override
            protected String getTextTrigger() {
                return decision.getText();
            }

            @Override
            public boolean appliesTo(GameState gameState, AwaitingDecision decision, String playerName) {
                return super.appliesTo(gameState, decision, playerName)
                        && decision.getDecisionParameters().get("source")[0].equals(source);
            }

            @Override
            public String getName() {
                return "AcsAnswerer-" + source + "-" + decision.getText();
            }
        };

        Map<AbstractArbitraryTrainer, AbstractArbitraryAnswerer> tbr = new HashMap<>();
        tbr.put(trainer, answerer);

        return tbr;
    }

    public static void makeAndRegisterTrainerAndAnswerer(AwaitingDecision decision) {
        if (!(decision instanceof ArbitraryCardsSelectionDecision)) {
            throw new IllegalArgumentException("Decision provided is not " + ArbitraryCardsSelectionDecision.class.getSimpleName());
        }

        Map<AbstractArbitraryTrainer, AbstractArbitraryAnswerer> pair = makeTrainerAnswererPair(decision);

        pair.values().forEach(AnswerersV2::addAnswerer);
        pair.forEach(TrainersV2::add);
    }
}
