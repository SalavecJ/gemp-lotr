package com.gempukku.lotro.bots.rl.v2.learning.cardselection.specific;

import com.gempukku.lotro.bots.rl.v2.learning.TrainerV2;
import com.gempukku.lotro.bots.rl.v2.learning.TrainersV2;
import com.gempukku.lotro.bots.rl.v2.learning.cardselection.AbstractCardSelectionTrainer;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.CardsSelectionDecision;

public class SpecificCardSelectionTrainerFactory {
    private SpecificCardSelectionTrainerFactory(){

    }

    private static AbstractCardSelectionTrainer makeTrainer(AwaitingDecision decision) {
        if (!(decision instanceof CardsSelectionDecision)) {
            throw new IllegalArgumentException("Decision provided is not " + CardsSelectionDecision.class.getSimpleName());
        }


        return new AbstractCardSelectionTrainer() {
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
            public boolean appliesTo(GameState gameState, AwaitingDecision decision1, String playerName) {
                return super.appliesTo(gameState, decision1, playerName)
                        && decision1.getDecisionParameters().get("source")[0].equals(source)
                        && getTextTrigger().equalsIgnoreCase(decision1.getText());
            }
        };
    }

    public  static void makeAndRegisterTrainer(AwaitingDecision decision) {
        if (!(decision instanceof CardsSelectionDecision)) {
            throw new IllegalArgumentException("Decision provided is not " + CardsSelectionDecision.class.getSimpleName());
        }

        TrainerV2 trainer = makeTrainer(decision);
        TrainersV2.add(trainer);
    }
}
