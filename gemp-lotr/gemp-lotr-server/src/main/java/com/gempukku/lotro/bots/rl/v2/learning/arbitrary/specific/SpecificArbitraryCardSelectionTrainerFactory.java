package com.gempukku.lotro.bots.rl.v2.learning.arbitrary.specific;

import com.gempukku.lotro.bots.rl.v2.learning.TrainerV2;
import com.gempukku.lotro.bots.rl.v2.learning.TrainersV2;
import com.gempukku.lotro.bots.rl.v2.learning.arbitrary.AbstractArbitraryTrainer;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

public class SpecificArbitraryCardSelectionTrainerFactory {
    private SpecificArbitraryCardSelectionTrainerFactory() {

    }

    private static AbstractArbitraryTrainer makeTrainer(AwaitingDecision decision) {
        if (!(decision instanceof ArbitraryCardsSelectionDecision)) {
            throw new IllegalArgumentException("Decision provided is not " + ArbitraryCardsSelectionDecision.class.getSimpleName());
        }

        return new AbstractArbitraryTrainer() {
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
            public boolean appliesTo(GameState gameState, AwaitingDecision decision, String playerName) {
                return super.appliesTo(gameState, decision, playerName)
                        && decision.getDecisionParameters().get("source")[0].equals(source);
            }
        };
    }


    public  static void makeAndRegisterTrainer(AwaitingDecision decision) {
        if (!(decision instanceof ArbitraryCardsSelectionDecision)) {
            throw new IllegalArgumentException("Decision provided is not " + ArbitraryCardsSelectionDecision.class.getSimpleName());
        }

        TrainerV2 trainer = makeTrainer(decision);
        TrainersV2.add(trainer);
    }
}
