package com.gempukku.lotro.bots.rl.v2.learning.cardaction.phase;

import com.gempukku.lotro.bots.rl.v2.learning.cardaction.AbstractCardActionTrainer;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.CardActionSelectionDecision;

import java.util.ArrayList;
import java.util.List;

public class ResponseCardActionTrainer extends AbstractPhaseCardActionTrainer {
    private final List<AbstractCardActionTrainer> specificTrainers = new ArrayList<>();

    private static final String TRIGGER = "responses";

    private final AbstractCardActionTrainer generalResponseTrainer = new AbstractCardActionTrainer() {
        @Override
        protected String getTextTrigger() {
            return TRIGGER;
        }

        @Override
        protected boolean containsRelevantDecision(GameState gameState, CardActionSelectionDecision decision, String playerName) {
            return decision.getDecisionParameters().get("actionId").length != 0;
        }

        @Override
        protected boolean relevantCardChosen(GameState gameState, CardActionSelectionDecision decision, String answer) {
            return !answer.isEmpty();
        }

        @Override
        public String getName() {
            return "CasGeneralTrainerResponse";
        }
    };

    @Override
    protected String getTextTrigger() {
        return TRIGGER;
    }

    @Override
    public List<AbstractCardActionTrainer> getSubTrainers() {
        ArrayList<AbstractCardActionTrainer> tbr = new ArrayList<>();
        tbr.add(generalResponseTrainer);
        tbr.addAll(specificTrainers);
        return tbr;
    }

    @Override
    public List<AbstractCardActionTrainer> getGeneralSubTrainers() {
        return List.of(generalResponseTrainer);
    }

    @Override
    public List<AbstractCardActionTrainer> getSpecificSubTrainers() {
        return specificTrainers;
    }

    @Override
    protected void addSpecificSubTrainer(AbstractCardActionTrainer trainer) {
        specificTrainers.add(trainer);
    }
}
