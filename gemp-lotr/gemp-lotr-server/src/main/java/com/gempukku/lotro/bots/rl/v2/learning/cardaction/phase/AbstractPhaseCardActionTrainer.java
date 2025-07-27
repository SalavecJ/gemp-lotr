package com.gempukku.lotro.bots.rl.v2.learning.cardaction.phase;

import com.gempukku.lotro.bots.rl.v2.ModelRegistryV2;
import com.gempukku.lotro.bots.rl.v2.learning.AbstractTrainerV2;
import com.gempukku.lotro.bots.rl.v2.learning.SavedVector;
import com.gempukku.lotro.bots.rl.v2.learning.cardaction.AbstractCardActionTrainer;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import com.gempukku.lotro.logic.decisions.CardActionSelectionDecision;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;

import java.util.*;

public abstract class AbstractPhaseCardActionTrainer extends AbstractTrainerV2 {

    protected abstract String getTextTrigger();
    public abstract List<AbstractCardActionTrainer> getSubTrainers();

    @Override
    public boolean appliesTo(GameState gameState, AwaitingDecision decision, String playerName) {
        if (!decision.getDecisionType().equals(AwaitingDecisionType.CARD_ACTION_CHOICE)) {
            return false;
        }

        if (!decision.getText().contains(getTextTrigger())) {
            return false;
        }

        Map<String, String[]> params = decision.getDecisionParameters();
        List<String> cardIds = Arrays.stream(params.get("cardId")).toList();
        List<String> blueprintIds = Arrays.stream(params.get("blueprintId")).toList();
        List<String> actionTexts = Arrays.stream(params.get("actionText")).toList();

        for (int i = 0; i < cardIds.size(); i++) {
            boolean anyMatch = false;
            CardActionSelectionDecision tmpDecision = new CardActionSelectionDecision(decision.getAwaitingDecisionId(),
                    decision.getText(),
                    List.of(cardIds.get(i)),
                    List.of(blueprintIds.get(i)),
                    List.of(actionTexts.get(i))) {
                @Override
                public void decisionMade(String result) throws DecisionResultInvalidException {

                }
            };
            for (AbstractCardActionTrainer subTrainer : getSubTrainers()) {
                if (subTrainer.appliesTo(gameState, tmpDecision, playerName)) {
                    anyMatch =true;
                    break;
                }
            }
            if (!anyMatch) {
                return false;
            }
        }

        return true;
    }

    @Override
    public String getAnswer(GameState gameState, AwaitingDecision decision, String playerName, ModelRegistryV2 modelRegistry) {
        Map<String, String[]> params = decision.getDecisionParameters();
        List<String> actionIds = Arrays.stream(params.get("actionId")).toList();
        List<String> cardIds = Arrays.stream(params.get("cardId")).toList();
        List<String> actionTexts = Arrays.stream(params.get("actionText")).toList();

        Map<String, IdActionPair> actions = new HashMap<>();
        for (int i = 0; i < actionIds.size(); i++) {
            actions.put(actionIds.get(i), new IdActionPair(cardIds.get(i), actionTexts.get(i)));
        }

        for (IdActionPair idActionPair : actions.values()) {
            for (AbstractCardActionTrainer trainer : getSubTrainers()) {
                if (trainer.appliesTo(gameState, decision, playerName)) {
                    trainer.scoreAction(gameState, decision, playerName, modelRegistry, idActionPair);
                }
            }
        }

        String answer = "";
        double bestConfidence = 0.0;
        for (Map.Entry<String, IdActionPair> entry : actions.entrySet()) {
            if (entry.getValue().confidence > bestConfidence) {
                answer = entry.getKey();
            }
        }
        return answer;
    }

    @Override
    public List<SavedVector> toStringVectors(GameState gameState, AwaitingDecision decision, String playerId, String answer) {
        List<SavedVector> tbr = new ArrayList<>();
        for (AbstractCardActionTrainer trainer : getSubTrainers()) {
            tbr.addAll(trainer.toStringVectors(gameState, decision, playerId, answer));
        }
        return tbr;
    }

    public static class IdActionPair {
        public String cardId;
        public String actionText;
        public double confidence = 0.0;

        public IdActionPair(String cardId, String actionText) {
            this.cardId = cardId;
            this.actionText = actionText;
        }
    }
}
