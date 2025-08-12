package com.gempukku.lotro.bots.rl.v2.learning.cardselection;

import com.gempukku.lotro.bots.BotService;
import com.gempukku.lotro.bots.rl.v2.ModelRegistryV2;
import com.gempukku.lotro.bots.rl.v2.learning.AbstractTrainerV2;
import com.gempukku.lotro.bots.rl.v2.learning.SavedVector;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.state.Skirmish;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import smile.classification.SoftClassifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public abstract class AbstractCardSelectionTrainer extends AbstractTrainerV2 {

    protected abstract String getTextTrigger();

    @Override
    public boolean appliesTo(GameState gameState, AwaitingDecision decision, String playerName) {
        if (decision.getDecisionType() != AwaitingDecisionType.CARD_SELECTION)
            return false;


        int min = Integer.parseInt(decision.getDecisionParameters().get("min")[0]);
        int max = Integer.parseInt(decision.getDecisionParameters().get("max")[0]);
        List<String> cardIds = Arrays.stream(decision.getDecisionParameters().get("cardId")).toList();
        if ((min == max && min == cardIds.size()) || max == 0) {
            // Had to choose all, nothing to learn from
            return false;
        }

        return decision.getText().toLowerCase().contains(getTextTrigger().toLowerCase());
    }

    @Override
    public String getAnswer(LotroGame game, AwaitingDecision decision, String playerName, ModelRegistryV2 modelRegistry) {
        // Always choose max
        int min = Integer.parseInt(decision.getDecisionParameters().get("min")[0]);
        int max = Integer.parseInt(decision.getDecisionParameters().get("max")[0]);
        List<String> cardIds = Arrays.stream(decision.getDecisionParameters().get("cardId")).toList();

        if (modelRegistry == null) {
            throw new UnsupportedOperationException("Model not found for " + getName());
        }
        SoftClassifier<double[]> model = modelRegistry.getModel(getName());
        if (model == null) {
            throw new UnsupportedOperationException("Model not found for " + getName());
        }

        // If in skirmish phase, and we should choose one card, choose skirmishing character if possible
        if (game.getGameState().getSkirmish() != null &&
                game.getGameState().getSkirmish().getFellowshipCharacter() != null &&
                game.getGameState().getSkirmish().getShadowCharacters() != null &&
                !game.getGameState().getSkirmish().getShadowCharacters().isEmpty()
                && min == 1 && max == 1) {
            Skirmish skirmish = game.getGameState().getSkirmish();
            for (String cardId : cardIds) {
                if (skirmish.getFellowshipCharacter().getCardId() == Integer.parseInt(cardId)) {
                    return cardId;
                }
                for (PhysicalCard shadowCharacter : skirmish.getShadowCharacters()) {
                    if (shadowCharacter.getCardId() == Integer.parseInt(cardId)) {
                        return cardId;
                    }
                }
            }
            // Skirmishing character cannot be chosen, continue
        }

        double[] stateVector = extractFeatures(game.getGameState(), decision, playerName);
        List<ScoredCard> scoredCards = new ArrayList<>();

        for (String physicalId : cardIds) {
            try {
                String blueprintId = game.getGameState().getBlueprintId(Integer.parseInt(physicalId));
                double[] cardVector = BotService.staticLibrary.getLotroCardBlueprint(blueprintId).getGeneralCardFeatures(game.getGameState(), Integer.parseInt(physicalId), playerName);
                double[] extended = Arrays.copyOf(stateVector, stateVector.length + cardVector.length);
                System.arraycopy(cardVector, 0, extended, stateVector.length, cardVector.length);

                double[] probs = new double[2];
                model.predict(extended, probs);
                scoredCards.add(new ScoredCard(physicalId, probs[1]));
            } catch (CardNotFoundException ignored) {

            }
        }

        scoredCards.sort(Comparator.comparingDouble(c -> -c.score));
        List<String> sortedIds = new ArrayList<>();
        scoredCards.forEach(scoredCard -> sortedIds.add(scoredCard.cardId));
        return String.join(",", sortedIds.subList(0, max));
    }

    @Override
    public List<SavedVector> toStringVectors(LotroGame game, AwaitingDecision decision, String playerId, String answer) {
        String className = getName();
        double[] state = extractFeatures(game.getGameState(), decision, playerId);

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
                for (PhysicalCard physicalCard : game.getGameState().getInPlay()) {
                    if (physicalCard.getCardId() == Integer.parseInt(chosenOption)) {
                        wounds = game.getGameState().getWounds(physicalCard);
                        break;
                    }
                }
                woundsOnChosen.add(wounds);
            }
        }

        List<Integer> woundsOnNotChosen = new ArrayList<>();
        for (String notChosenOption : notChosenOptions) {
            int wounds = 0;
            for (PhysicalCard physicalCard : game.getGameState().getInPlay()) {
                if (physicalCard.getCardId() == Integer.parseInt(notChosenOption)) {
                    wounds = game.getGameState().getWounds(physicalCard);
                    break;
                }
            }
            woundsOnNotChosen.add(wounds);
        }

        List<double[]> notChosenVectors = new ArrayList<>();
        for (int i = 0; i < notChosenOptions.size(); i++) {
            String notChosenOption = notChosenOptions.get(i);
            try {
                double[] cardVector = BotService.staticLibrary.getLotroCardBlueprint(game.getGameState().getBlueprintId(Integer.parseInt(notChosenOption))).getGeneralCardFeatures(game.getGameState(), -1, playerId, woundsOnNotChosen.get(i));
                notChosenVectors.add(cardVector);
            } catch (CardNotFoundException ignored) {

            }
        }

        List<SavedVector> tbr = new ArrayList<>();
        if (!chosenOptions.contains("")) { // Pass
            for (int i = 0; i < chosenOptions.size(); i++) {
                String chosenOption = chosenOptions.get(i);
                try {
                    double[] cardVector = BotService.staticLibrary.getLotroCardBlueprint(game.getGameState().getBlueprintId(Integer.parseInt(chosenOption))).getGeneralCardFeatures(game.getGameState(), -1, playerId, woundsOnChosen.get(i));
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
