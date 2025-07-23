package com.gempukku.lotro.bots.rl.v2.decisions.choice.specific;

import com.gempukku.lotro.bots.rl.v2.decisions.AnswerersV2;
import com.gempukku.lotro.bots.rl.v2.decisions.choice.AbstractChoiceAnswerer;
import com.gempukku.lotro.bots.rl.v2.learning.TrainersV2;
import com.gempukku.lotro.bots.rl.v2.learning.choice.AbstractChoiceTrainer;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import com.gempukku.lotro.logic.decisions.MultipleChoiceAwaitingDecision;

import java.util.*;

public class SpecificChoiceFactory {
    private SpecificChoiceFactory() {

    }

    private static Map<AbstractChoiceTrainer, AbstractChoiceAnswerer> makeTrainerAnswererPair(AwaitingDecision decision) {
        if (!(decision instanceof MultipleChoiceAwaitingDecision)) {
            throw new IllegalArgumentException("Decision provided is not " + MultipleChoiceAwaitingDecision.class.getSimpleName());
        }

        List<String> cardOptions = Arrays.stream(decision.getDecisionParameters().get("results")).toList();

        AbstractChoiceTrainer trainer = new AbstractChoiceTrainer() {
            @Override
            public boolean isDecisionRelevant(GameState gameState, AwaitingDecision decision, String playerName) {
                if (decision.getDecisionType() != AwaitingDecisionType.MULTIPLE_CHOICE)
                    return false;

                String[] options = decision.getDecisionParameters().get("results");
                if (options.length != cardOptions.size())
                    return false;

                List<String> inputOptions = Arrays.stream(options).map(String::trim).toList();
                List<String> normalizedCardOptions = cardOptions.stream().map(String::trim).toList();

                return new HashSet<>(inputOptions).equals(new HashSet<>(normalizedCardOptions));
            }

            @Override
            protected String getTextTrigger() {
                return null;
            }

            @Override
            protected int getNumberOfOptions() {
                return cardOptions.size();
            }

            @Override
            public String getName() {
                return "McTrainer" + cardOptions;
            }
        };

        AbstractChoiceAnswerer answerer = new AbstractChoiceAnswerer() {
            @Override
            public boolean appliesTo(GameState gameState, AwaitingDecision decision, String playerName) {
                if (decision.getDecisionType() != AwaitingDecisionType.MULTIPLE_CHOICE)
                    return false;

                String[] options = decision.getDecisionParameters().get("results");
                if (options.length != cardOptions.size())
                    return false;

                List<String> inputOptions = Arrays.stream(options).map(String::trim).toList();
                List<String> normalizedCardOptions = cardOptions.stream().map(String::trim).toList();

                return new HashSet<>(inputOptions).equals(new HashSet<>(normalizedCardOptions));
            }

            @Override
            protected String getTextTrigger() {
                return null;
            }

            @Override
            protected List<String> getOptions() {
                return cardOptions;
            }

            @Override
            public String getName() {
                return "McAnswerer" + cardOptions;
            }
        };

        Map<AbstractChoiceTrainer, AbstractChoiceAnswerer> tbr = new HashMap<>();
        tbr.put(trainer, answerer);

        return tbr;
    }

    public static void makeAndRegisterTrainerAndAnswerer(AwaitingDecision decision) {
        if (!(decision instanceof MultipleChoiceAwaitingDecision)) {
            throw new IllegalArgumentException("Decision provided is not " + MultipleChoiceAwaitingDecision.class.getSimpleName());
        }

        Map<AbstractChoiceTrainer, AbstractChoiceAnswerer> pair = makeTrainerAnswererPair(decision);

        pair.values().forEach(AnswerersV2::addAnswerer);
        pair.forEach(TrainersV2::add);
    }

}
