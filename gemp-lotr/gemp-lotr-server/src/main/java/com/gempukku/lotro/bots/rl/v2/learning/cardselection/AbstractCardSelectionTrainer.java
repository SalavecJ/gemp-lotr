package com.gempukku.lotro.bots.rl.v2.learning.cardselection;

import com.gempukku.lotro.bots.BotService;
import com.gempukku.lotro.bots.rl.v2.learning.AbstractTrainerV2;
import com.gempukku.lotro.bots.rl.v2.learning.SavedVector;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractCardSelectionTrainer extends AbstractTrainerV2 {

    protected abstract String getTextTrigger();

    @Override
    public boolean isDecisionRelevant(GameState gameState, AwaitingDecision decision, String playerName) {
        if (decision.getDecisionType() != AwaitingDecisionType.CARD_SELECTION)
            return false;


        int min = Integer.parseInt(decision.getDecisionParameters().get("min")[0]);
        int max = Integer.parseInt(decision.getDecisionParameters().get("max")[0]);
        List<String> cardIds = Arrays.stream(decision.getDecisionParameters().get("cardId")).toList();
        if (min == max && min == cardIds.size()) {
            // Had to choose all, nothing to learn from
            return false;
        }

        return decision.getText().toLowerCase().contains(getTextTrigger().toLowerCase());
    }

    @Override
    public List<SavedVector> toStringVectors(GameState gameState, AwaitingDecision decision, String playerId, String answer) {
        String className = getName();
        double[] state = extractFeatures(gameState, decision, playerId);

        List<String> chosenOptions = Arrays.stream(answer.split(",")).toList();
        List<String> notChosenOptions = new ArrayList<>();
        for (String cardId : decision.getDecisionParameters().get("cardId")) {
            if (!chosenOptions.contains(cardId)) {
                notChosenOptions.add(cardId);
            }
        }

        List<Integer> woundsOnChosen = new ArrayList<>();
        if (!chosenOptions.contains("")) { // Pass
            for (String chosenOption : chosenOptions) {
                int wounds = 0;
                for (PhysicalCard physicalCard : gameState.getInPlay()) {
                    if (physicalCard.getCardId() == Integer.parseInt(chosenOption)) {
                        wounds = gameState.getWounds(physicalCard);
                        break;
                    }
                }
                woundsOnChosen.add(wounds);
            }
        }

        List<Integer> woundsOnNotChosen = new ArrayList<>();
        for (String notChosenOption : notChosenOptions) {
            int wounds = 0;
            for (PhysicalCard physicalCard : gameState.getInPlay()) {
                if (physicalCard.getCardId() == Integer.parseInt(notChosenOption)) {
                    wounds = gameState.getWounds(physicalCard);
                    break;
                }
            }
            woundsOnNotChosen.add(wounds);
        }

        List<double[]> notChosenVectors = new ArrayList<>();
        for (int i = 0; i < notChosenOptions.size(); i++) {
            String notChosenOption = notChosenOptions.get(i);
            try {
                double[] cardVector = BotService.staticLibrary.getLotroCardBlueprint(gameState.getBlueprintId(Integer.parseInt(notChosenOption))).getGeneralCardFeatures(gameState, -1, playerId, woundsOnNotChosen.get(i));
                notChosenVectors.add(cardVector);
            } catch (CardNotFoundException ignored) {

            }
        }

        List<SavedVector> tbr = new ArrayList<>();
        if (!chosenOptions.contains("")) { // Pass
            for (int i = 0; i < chosenOptions.size(); i++) {
                String chosenOption = chosenOptions.get(i);
                try {
                    double[] cardVector = BotService.staticLibrary.getLotroCardBlueprint(gameState.getBlueprintId(Integer.parseInt(chosenOption))).getGeneralCardFeatures(gameState, -1, playerId, woundsOnChosen.get(i));
                    tbr.add(new SavedVector(className, state, cardVector, notChosenVectors));
                } catch (CardNotFoundException ignored) {

                }
            }
        } else if (!notChosenVectors.isEmpty()){
            int vectorSize = notChosenVectors.getFirst().length;
            double[] cardVector = new double[vectorSize];
            tbr.add(new SavedVector(className, state, cardVector, notChosenVectors));
        }

        return tbr;
    }

}
