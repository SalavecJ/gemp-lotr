package com.gempukku.lotro.bots.rl.v2.decisions.cardselection.specific;

import com.gempukku.lotro.bots.rl.learning.LearningStep;
import com.gempukku.lotro.bots.rl.v2.decisions.AnswerersV2;
import com.gempukku.lotro.bots.rl.v2.decisions.cardselection.AbstractCardSelectionAnswerer;
import com.gempukku.lotro.bots.rl.v2.learning.TrainersV2;
import com.gempukku.lotro.bots.rl.v2.learning.cardselection.AbstractCardSelectionTrainer;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.CardsSelectionDecision;

import java.util.HashMap;
import java.util.Map;

public class SpecificCardSelectionFactory {
    private SpecificCardSelectionFactory(){

    }

    private static Map<AbstractCardSelectionTrainer, AbstractCardSelectionAnswerer> makeTrainerAnswererPair(AwaitingDecision decision) {
        if (!(decision instanceof CardsSelectionDecision)) {
            throw new IllegalArgumentException("Decision provided is not " + CardsSelectionDecision.class.getSimpleName());
        }

        AbstractCardSelectionTrainer trainer = new AbstractCardSelectionTrainer() {
            private final String source = decision.getDecisionParameters().get("source")[0];
            @Override
            protected String getTextTrigger() {
                return decision.getText();
            }

            @Override
            public String getName() {
                return "CsTrainer-" + source + "-" + decision.getText();
            }

            @Override
            public boolean isStepRelevant(LearningStep step) {
                return super.isStepRelevant(step)
                        && step.decision.getDecisionParameters().get("source")[0].equals(source)
                        && getTextTrigger().equalsIgnoreCase(step.decision.getText());
            }
        };

        AbstractCardSelectionAnswerer answerer = new AbstractCardSelectionAnswerer() {
            private final String source = decision.getDecisionParameters().get("source")[0];
            @Override
            protected String getTextTrigger() {
                return decision.getText();
            }

            @Override
            public boolean appliesTo(GameState gameState, AwaitingDecision decision, String playerName) {
                return super.appliesTo(gameState, decision, playerName)
                        && decision.getDecisionParameters().get("source")[0].equals(source)
                        && getTextTrigger().equalsIgnoreCase(decision.getText());
            }

            @Override
            public String getName() {
                return "CsAnswerer-" + source + "-" + decision.getText();
            }
        };

        Map<AbstractCardSelectionTrainer, AbstractCardSelectionAnswerer> tbr = new HashMap<>();
        tbr.put(trainer, answerer);

        return tbr;
    }

    public  static void makeAndRegisterTrainerAndAnswerer(AwaitingDecision decision) {
        if (!(decision instanceof CardsSelectionDecision)) {
            throw new IllegalArgumentException("Decision provided is not " + CardsSelectionDecision.class.getSimpleName());
        }

        Map<AbstractCardSelectionTrainer, AbstractCardSelectionAnswerer> pair = makeTrainerAnswererPair(decision);

        pair.values().forEach(AnswerersV2::addAnswerer);
        pair.forEach(TrainersV2::add);
    }
}
