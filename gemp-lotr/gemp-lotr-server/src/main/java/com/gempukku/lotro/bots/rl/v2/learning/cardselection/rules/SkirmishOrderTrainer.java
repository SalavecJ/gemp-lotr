package com.gempukku.lotro.bots.rl.v2.learning.cardselection.rules;

import com.gempukku.lotro.bots.BotService;
import com.gempukku.lotro.bots.rl.learning.LabeledPoint;
import com.gempukku.lotro.bots.rl.learning.LearningStep;
import com.gempukku.lotro.bots.rl.learning.semanticaction.CardSelectionAction;
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
    protected List<LabeledPoint> extractTrainingData(List<SavedVector> vectors) {

        // Does not use vector.notChosen

        List<LabeledPoint> data = new ArrayList<>();

        for (SavedVector vector : vectors) {
            if (!vector.className.equals(getName()))
                continue;

            int label = vector.reward > 0 ? 1 : 0;
            data.add(new LabeledPoint(label, append(vector.state, vector.chosen)));
        }

        return data;
    }

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

        // Empty, does not get use during data extraction
        List<double[]> notChosenVectors = new ArrayList<>();

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
