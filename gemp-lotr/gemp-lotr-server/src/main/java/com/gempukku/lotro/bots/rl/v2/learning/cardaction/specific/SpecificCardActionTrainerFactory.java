package com.gempukku.lotro.bots.rl.v2.learning.cardaction.specific;

import com.gempukku.lotro.bots.BotService;
import com.gempukku.lotro.bots.rl.v2.learning.TrainersV2;
import com.gempukku.lotro.bots.rl.v2.learning.cardaction.AbstractCardActionTrainer;
import com.gempukku.lotro.cards.evaluation.CardEvaluators;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.decisions.CardActionSelectionDecision;

import java.util.function.Predicate;

public class SpecificCardActionTrainerFactory {
    private SpecificCardActionTrainerFactory() {

    }

    private static AbstractCardActionTrainer makeTrainer(String phaseTrigger, String actionStartsWith, String blueprintId) {

        return new AbstractCardActionTrainer() {
            @Override
            protected String getTextTrigger() {
                return phaseTrigger;
            }

            @Override
            protected boolean containsRelevantDecision(GameState gameState, CardActionSelectionDecision decision, String playerName) {
                for (int i = 0; i < decision.getDecisionParameters().get("actionText").length; i++) {
                    if (decision.getDecisionParameters().get("actionText")[i].startsWith(actionStartsWith)
                            && decision.getDecisionParameters().get("realBlueprintId")[i].equals(blueprintId)) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            protected boolean relevantCardChosen(GameState gameState, CardActionSelectionDecision decision, String answer) {
                return decision.getDecisionParameters().get("actionText")[Integer.parseInt(answer)].startsWith(actionStartsWith)
                        && decision.getDecisionParameters().get("realBlueprintId")[Integer.parseInt(answer)].equals(blueprintId);
            }

            @Override
            public String getName() {
                return "CasTrainer-" + blueprintId + "-" + getTextTrigger() + "-" + actionStartsWith;
            }

            @Override
            protected boolean optionDoesAnything(LotroGame game, int cardId, String playerName) {
                switch (actionStartsWith) {
                    case "Play" -> {
                        return CardEvaluators.doesAnythingIfPlayed(game, cardId, playerName, game.getGameState().getPhysicalCard(cardId).getBlueprint());
                    }
                    case "Use", "Optional" -> {
                        return CardEvaluators.doesAnythingIfUsed(game, cardId, playerName, game.getGameState().getPhysicalCard(cardId).getBlueprint());
                    }
                    case "Heal" -> {
                        PhysicalCard toBeHealed = game.getGameState().getInPlay().stream().filter((Predicate<PhysicalCard>) physicalCard -> physicalCard.getBlueprintId().equals(blueprintId)).findFirst().orElseThrow();
                        return game.getModifiersQuerying().canBeHealed(game, toBeHealed);
                    }
                    case "Transfer" -> {
                        return true;
                    }
                    default ->
                            throw new IllegalArgumentException("Cannot check if action does anything for trainer: " + getName());
                }
            }

            @Override
            protected double[] getCardVector(LotroGame game, int cardId, String blueprintId, String playerName) throws CardNotFoundException {
                return switch (actionStartsWith) {
                    case "Play" ->
                            BotService.staticLibrary.getLotroCardBlueprint(blueprintId).getSpecificPlayFromHandCardFeatures(game, cardId, playerName);
                    case "Use" ->
                            BotService.staticLibrary.getLotroCardBlueprint(blueprintId).getSpecificUseCardFeatures(game, cardId, playerName);
                    case "Optional" ->
                            BotService.staticLibrary.getLotroCardBlueprint(blueprintId).getSpecificTriggerCardFeatures(game, cardId, playerName);
                    default -> super.getCardVector(game, cardId, blueprintId, playerName);
                };
            }
        };
    }

    public  static AbstractCardActionTrainer makeAndRegisterTrainer(String phaseTrigger, String actionStartsWith, String blueprintId) {
        AbstractCardActionTrainer trainer = makeTrainer(phaseTrigger, actionStartsWith, blueprintId);
        TrainersV2.addSubTrainer(trainer);

        return trainer;
    }
}
