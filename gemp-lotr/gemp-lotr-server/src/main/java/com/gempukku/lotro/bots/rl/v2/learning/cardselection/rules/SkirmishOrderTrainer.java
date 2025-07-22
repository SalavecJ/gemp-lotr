package com.gempukku.lotro.bots.rl.v2.learning.cardselection.rules;

import com.gempukku.lotro.bots.BotService;
import com.gempukku.lotro.bots.rl.learning.LearningStep;
import com.gempukku.lotro.bots.rl.learning.semanticaction.CardSelectionAssignedAction;
import com.gempukku.lotro.bots.rl.learning.semanticaction.SemanticAction;
import com.gempukku.lotro.bots.rl.v2.learning.SavedVector;
import com.gempukku.lotro.bots.rl.v2.learning.cardselection.AbstractCardSelectionTrainer;
import com.gempukku.lotro.bots.rl.v2.state.SkirmishOrderStateExtractor;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

import java.util.ArrayList;
import java.util.List;

public class SkirmishOrderTrainer extends AbstractCardSelectionTrainer {

    @Override
    protected String getTextTrigger() {
        return "next skirmish to resolve";
    }

    @Override
    public double[] extractFeatures(GameState gameState, AwaitingDecision decision, String playerName) {
        return SkirmishOrderStateExtractor.extractFeatures(gameState, decision, playerName);
    }

    @Override
    public boolean isStepRelevant(LearningStep step) {
        return super.isStepRelevant(step) && (step.action instanceof CardSelectionAssignedAction);
    }

    @Override
    public List<SavedVector> toStringVectors(GameState gameState, SemanticAction action, String playerId, AwaitingDecision decision) {
        String className = getName();
        double[] state = extractFeatures(gameState, decision, playerId);

        List<String> chosenOptions = ((CardSelectionAssignedAction) action).getChosenBlueprintIds();
        List<Integer> woundsOnChosen = ((CardSelectionAssignedAction) action).getWoundsOnChosen();
        List<Integer> minionsOnChosen = ((CardSelectionAssignedAction) action).getMinionsOnChosen();
        List<Integer> strengthOfMinionsOnChosen = ((CardSelectionAssignedAction) action).getStrengthOfMinionsOnChosen();
        List<String> notChosenOptions = ((CardSelectionAssignedAction) action).getNotChosenBlueprintIds();
        List<Integer> woundsOnNotChosen = ((CardSelectionAssignedAction) action).getWoundsOnNotChosen();
        List<Integer> minionsOnNotChosen = ((CardSelectionAssignedAction) action).getMinionsOnNotChosen();
        List<Integer> strengthOfMinionsOnNotChosen = ((CardSelectionAssignedAction) action).getStrengthOfMinionsOnNotChosen();

        List<double[]> notChosenVectors = new ArrayList<>();
        for (int i = 0; i < notChosenOptions.size(); i++) {
            String notChosenOption = notChosenOptions.get(i);
            try {
                double[] cardVector = BotService.staticLibrary.getLotroCardBlueprint(notChosenOption).getFpAssignedCardFeatures(gameState, -1, playerId, woundsOnNotChosen.get(i), minionsOnNotChosen.get(i), strengthOfMinionsOnNotChosen.get(i));
                notChosenVectors.add(cardVector);
            } catch (CardNotFoundException ignored) {

            }
        }

        List<SavedVector> tbr = new ArrayList<>();
        for (int i = 0; i < chosenOptions.size(); i++) {
            String chosenOption = chosenOptions.get(i);
            try {
                double[] cardVector = BotService.staticLibrary.getLotroCardBlueprint(chosenOption).getFpAssignedCardFeatures(gameState, -1, playerId, woundsOnChosen.get(i), minionsOnChosen.get(i), strengthOfMinionsOnChosen.get(i));
                tbr.add(new SavedVector(className, state, cardVector, notChosenVectors));
            } catch (CardNotFoundException ignored) {

            }
        }

        return tbr;
    }
}
