package com.gempukku.lotro.bots.rl.v2.learning.cardselection.rules;

import com.gempukku.lotro.bots.BotService;
import com.gempukku.lotro.bots.rl.v2.learning.SavedVector;
import com.gempukku.lotro.bots.rl.v2.learning.cardselection.AbstractCardSelectionTrainer;
import com.gempukku.lotro.bots.rl.v2.state.SkirmishOrderStateExtractor;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.Assignment;
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
    public List<SavedVector> toStringVectors(GameState gameState, AwaitingDecision decision, String playerId, String answer) {
        String className = getName();
        double[] state = extractFeatures(gameState, decision, playerId);

        PhysicalCard fpCard = null;
        for (PhysicalCard physicalCard : gameState.getInPlay()) {
            if (physicalCard.getCardId() == Integer.parseInt(answer)) {
                fpCard = physicalCard;
                break;
            }
        }

        if (fpCard == null) {
            throw new IllegalArgumentException("Could not find chosen card in game state.");
        }

        int woundsOnChosen = gameState.getWounds(fpCard);
        int minionsOnChosen = 0;
        int strengthOfMinionsOnChosen = 0;
        for (Assignment assignment : gameState.getAssignments()) {
            if (assignment.getFellowshipCharacter().getCardId() == fpCard.getCardId()) {
                minionsOnChosen = assignment.getShadowCharacters().size();
                strengthOfMinionsOnChosen = assignment.getShadowCharacters().stream().mapToInt(value -> value.getBlueprint().getStrength()).sum();
                break;
            }
        }

        List<String> notChosenOptions = new ArrayList<>();
        for (String cardId : decision.getDecisionParameters().get("cardId")) {
            if (!answer.equals(cardId)) {
                notChosenOptions.add(cardId);
            }
        }
        List<Integer> woundsOnNotChosen = new ArrayList<>();
        List<Integer> minionsOnNotChosen = new ArrayList<>();
        List<Integer> strengthOfMinionsOnNotChosen = new ArrayList<>();
        for (String notChosenOption : notChosenOptions) {
            int wounds = 0;
            int minions = 0;
            int strengthOfMinions = 0;
            for (PhysicalCard physicalCard : gameState.getInPlay()) {
                if (physicalCard.getCardId() == Integer.parseInt(notChosenOption)) {
                    wounds = gameState.getWounds(physicalCard);

                    for (Assignment assignment : gameState.getAssignments()) {
                        if (assignment.getFellowshipCharacter().getCardId() == physicalCard.getCardId()) {
                            minions = assignment.getShadowCharacters().size();
                            strengthOfMinions = assignment.getShadowCharacters().stream().mapToInt(value -> value.getBlueprint().getStrength()).sum();
                            break;
                        }
                    }
                    break;
                }
            }
            woundsOnNotChosen.add(wounds);
            minionsOnNotChosen.add(minions);
            strengthOfMinionsOnNotChosen.add(strengthOfMinions);
        }

        List<double[]> notChosenVectors = new ArrayList<>();
        for (int i = 0; i < notChosenOptions.size(); i++) {
            String notChosenOption = notChosenOptions.get(i);
            try {
                double[] cardVector = BotService.staticLibrary.getLotroCardBlueprint(getBlueprintId(notChosenOption, gameState)).getFpAssignedCardFeatures(gameState, -1, playerId, woundsOnNotChosen.get(i), minionsOnNotChosen.get(i), strengthOfMinionsOnNotChosen.get(i));
                notChosenVectors.add(cardVector);
            } catch (CardNotFoundException ignored) {

            }
        }

        List<SavedVector> tbr = new ArrayList<>();
        try {
            double[] cardVector = BotService.staticLibrary.getLotroCardBlueprint(getBlueprintId(answer, gameState)).getFpAssignedCardFeatures(gameState, -1, playerId, woundsOnChosen, minionsOnChosen, strengthOfMinionsOnChosen);
            tbr.add(new SavedVector(className, state, cardVector, notChosenVectors));
        } catch (CardNotFoundException ignored) {

        }

        return tbr;
    }

    private String getBlueprintId(String physicalId, GameState gameState) throws CardNotFoundException {
        for (PhysicalCard card : gameState.getAllCards()) {
            if (card.getCardId() == Integer.parseInt(physicalId)) {
                return card.getBlueprintId();
            }
        }
        throw new CardNotFoundException("Card not found in game state");
    }
}
