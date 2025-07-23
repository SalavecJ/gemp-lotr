package com.gempukku.lotro.bots.rl.v2.learning.arbitrary;

import com.gempukku.lotro.bots.BotService;
import com.gempukku.lotro.bots.rl.v2.learning.AbstractTrainerV2;
import com.gempukku.lotro.bots.rl.v2.learning.SavedVector;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;

import java.util.*;

public abstract class AbstractArbitraryTrainer extends AbstractTrainerV2 {
    protected abstract String getTextTrigger();

    @Override
    public boolean isDecisionRelevant(GameState gameState, AwaitingDecision decision, String playerName) {
        if (decision.getDecisionType() != AwaitingDecisionType.ARBITRARY_CARDS)
            return false;

        return decision.getText().toLowerCase().contains(getTextTrigger().toLowerCase());
    }

    @Override
    public List<SavedVector> toStringVectors(GameState gameState, AwaitingDecision decision, String playerId, String answer) {
        String className = getName();
        double[] state = extractFeatures(gameState, decision, playerId);

        List<String> chosenIds = Arrays.stream(answer.split(",")).toList();
        Map<String, String> chosenOptions = new HashMap<>();
        List<String> blueprintIds = Arrays.stream(decision.getDecisionParameters().get("blueprintId")).toList();
        List<String> cardIds = Arrays.stream(decision.getDecisionParameters().get("cardId")).toList();
        List<String> selectable = Arrays.stream(decision.getDecisionParameters().get("selectable")).toList();

        List<String> selectableIds = new ArrayList<>();
        for (int i = 0; i < cardIds.size() && i < selectable.size(); i++) {
            if (Boolean.parseBoolean(selectable.get(i))) {
                selectableIds.add(cardIds.get(i));
            }
        }

        for (int i = 0; i < cardIds.size(); i++) {
            String cardId = cardIds.get(i);
            if (chosenIds.contains(cardId)) {
                chosenOptions.put(cardId, blueprintIds.get(i));
            }
        }

        Map<String, String> notChosenOptions = new HashMap<>();
        for (int i = 0; i < cardIds.size(); i++) {
            String cardId = cardIds.get(i);
            if (selectableIds.contains(cardId) && !chosenOptions.containsKey(cardId)) {
                notChosenOptions.put(cardId, blueprintIds.get(i));
            }
        }

        List<double[]> notChosenVectors = new ArrayList<>();
        for (Map.Entry<String, String> entry : notChosenOptions.entrySet()) {
            try {
                double[] cardVector = BotService.staticLibrary.getLotroCardBlueprint(entry.getValue()).getGeneralCardFeatures(gameState, -1, playerId);
                notChosenVectors.add(cardVector);
            } catch (CardNotFoundException ignored) {

            }

        }

        List<SavedVector> tbr = new ArrayList<>();

        for (Map.Entry<String, String> entry : chosenOptions.entrySet()) {
            try {
                double[] cardVector = BotService.staticLibrary.getLotroCardBlueprint(entry.getValue()).getGeneralCardFeatures(gameState, -1, playerId);
                tbr.add(new SavedVector(className, state, cardVector, notChosenVectors));
            } catch (CardNotFoundException ignored) {

            }
        }

        return tbr;
    }
}
