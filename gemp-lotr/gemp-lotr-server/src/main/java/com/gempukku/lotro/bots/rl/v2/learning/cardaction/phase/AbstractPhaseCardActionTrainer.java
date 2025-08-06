package com.gempukku.lotro.bots.rl.v2.learning.cardaction.phase;

import com.gempukku.lotro.bots.rl.v2.ModelRegistryV2;
import com.gempukku.lotro.bots.rl.v2.learning.AbstractTrainerV2;
import com.gempukku.lotro.bots.rl.v2.learning.SavedVector;
import com.gempukku.lotro.bots.rl.v2.learning.cardaction.AbstractCardActionTrainer;
import com.gempukku.lotro.bots.rl.v2.learning.cardaction.specific.SpecificCardActionTrainerFactory;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import com.gempukku.lotro.logic.decisions.CardActionSelectionDecision;

import java.util.*;

public abstract class AbstractPhaseCardActionTrainer extends AbstractTrainerV2 {

    protected abstract String getTextTrigger();
    public abstract List<AbstractCardActionTrainer> getSubTrainers();
    public abstract List<AbstractCardActionTrainer> getGeneralSubTrainers();
    public abstract List<AbstractCardActionTrainer> getSpecificSubTrainers();
    protected abstract void addSpecificSubTrainer(AbstractCardActionTrainer trainer);

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
        List<String> realBlueprintIds = Arrays.stream(params.get("realBlueprintId")).toList();

        for (int i = 0; i < cardIds.size(); i++) {
            boolean anyMatch = false;
            CardActionSelectionDecision tmpDecision = new CardActionSelectionDecision(decision.getAwaitingDecisionId(),
                    decision.getText(),
                    List.of(cardIds.get(i)),
                    List.of(blueprintIds.get(i)),
                    List.of(actionTexts.get(i)),
                    List.of(realBlueprintIds.get(i))) {
                @Override
                public void decisionMade(String result) {

                }
            };
            for (AbstractCardActionTrainer subTrainer : getSubTrainers()) {
                if (subTrainer.appliesTo(gameState, tmpDecision, playerName)) {
                    anyMatch = true;
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
    public String getAnswer(LotroGame game, AwaitingDecision decision, String playerName, ModelRegistryV2 modelRegistry) {
        Map<String, String[]> params = decision.getDecisionParameters();
        List<String> actionIds = Arrays.stream(params.get("actionId")).toList();
        List<String> cardIds = Arrays.stream(params.get("cardId")).toList();
        List<String> blueprintIds = Arrays.stream(params.get("blueprintId")).toList();
        List<String> actionTexts = Arrays.stream(params.get("actionText")).toList();
        List<String> realBlueprintIds = Arrays.stream(params.get("realBlueprintId")).toList();

        Map<String, IdActionPair> actions = new HashMap<>();
        for (int i = 0; i < actionIds.size(); i++) {
            actions.put(actionIds.get(i), new IdActionPair(cardIds.get(i), blueprintIds.get(i), realBlueprintIds.get(i), actionTexts.get(i)));
        }

        for (IdActionPair idActionPair : actions.values()) {
            CardActionSelectionDecision tmpDecision = new CardActionSelectionDecision(decision.getAwaitingDecisionId(),
                    decision.getText(),
                    List.of(idActionPair.cardId),
                    List.of(idActionPair.blueprintId),
                    List.of(idActionPair.actionText),
                    List.of(idActionPair.realBlueprintId)) {
                @Override
                public void decisionMade(String result) {

                }
            };
            boolean answeredBySpecific = false;

            for (AbstractCardActionTrainer trainer : getSpecificSubTrainers()) {
                if (trainer.appliesTo(game.getGameState(), tmpDecision, playerName)) {
                    try {
                        trainer.scoreAction(game, decision, playerName, modelRegistry, idActionPair);
                        answeredBySpecific = true;
                    } catch (UnsupportedOperationException ignored) {
                        // Fallback to general trainers if trainer is made, but model not trained
                    }
                }
            }

            if (!answeredBySpecific) {
                for (AbstractCardActionTrainer trainer : getGeneralSubTrainers()) {
                    if (trainer.appliesTo(game.getGameState(), tmpDecision, playerName)) {
                        trainer.scoreAction(game, decision, playerName, modelRegistry, idActionPair);
                    }
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
    public List<SavedVector> toStringVectors(LotroGame game, AwaitingDecision decision, String playerId, String answer) {
        List<SavedVector> tbr = new ArrayList<>();

        for (AbstractCardActionTrainer trainer : getSpecificSubTrainers()) {
            tbr.addAll(trainer.toStringVectors(game, decision, playerId, answer));
        }

        if (tbr.isEmpty() && !answer.isEmpty()) {
            String actionPrefix = decision.getDecisionParameters().get("actionText")[Integer.parseInt(answer)].split(" ", 2)[0];
            String realBlueprintId = decision.getDecisionParameters().get("realBlueprintId")[Integer.parseInt(answer)];
            AbstractCardActionTrainer trainer = SpecificCardActionTrainerFactory.makeAndRegisterTrainer(getTextTrigger(), actionPrefix, realBlueprintId);
            addSpecificSubTrainer(trainer);
            tbr.addAll(trainer.toStringVectors(game, decision, playerId, answer));
        }

        for (AbstractCardActionTrainer trainer : getGeneralSubTrainers()) {
            tbr.addAll(trainer.toStringVectors(game, decision, playerId, answer));
        }

        return tbr;
    }

    public static class IdActionPair {
        public String cardId;
        public String blueprintId;
        public String realBlueprintId;
        public String actionText;
        public double confidence = 0.0;

        public IdActionPair(String cardId, String blueprintId, String realBlueprintId, String actionText) {
            this.cardId = cardId;
            this.blueprintId = blueprintId;
            this.realBlueprintId = realBlueprintId;
            this.actionText = actionText;
        }
    }
}
