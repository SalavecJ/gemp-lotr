package com.gempukku.lotro.bots.rl.v2.learning.choice.specific;

import com.gempukku.lotro.bots.rl.v2.learning.TrainerV2;
import com.gempukku.lotro.bots.rl.v2.learning.TrainersV2;
import com.gempukku.lotro.bots.rl.v2.learning.choice.AbstractChoiceTrainer;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import com.gempukku.lotro.logic.decisions.MultipleChoiceAwaitingDecision;

import java.util.*;

public class SpecificChoiceTrainerFactory {
    private SpecificChoiceTrainerFactory() {

    }

    private static AbstractChoiceTrainer makeTrainer(AwaitingDecision decision) {
        if (!(decision instanceof MultipleChoiceAwaitingDecision)) {
            throw new IllegalArgumentException("Decision provided is not " + MultipleChoiceAwaitingDecision.class.getSimpleName());
        }

        List<String> cardOptions = Arrays.stream(decision.getDecisionParameters().get("results")).toList();

        return new AbstractChoiceTrainer() {
            //TODO add source
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
            protected int getNumberOfOptions() {
                return cardOptions.size();
            }

            @Override
            public String getName() {
                return "McTrainer" + cardOptions;
            }
        };
    }


    public  static void makeAndRegisterTrainer(AwaitingDecision decision) {
        if (!(decision instanceof MultipleChoiceAwaitingDecision)) {
            throw new IllegalArgumentException("Decision provided is not " + MultipleChoiceAwaitingDecision.class.getSimpleName());
        }

        TrainerV2 trainer = makeTrainer(decision);
        TrainersV2.add(trainer);
    }
}
