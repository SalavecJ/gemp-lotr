package com.gempukku.lotro.bots.rl.v2.learning.cardaction.specific;

import com.gempukku.lotro.bots.BotService;
import com.gempukku.lotro.bots.rl.v2.learning.TrainersV2;
import com.gempukku.lotro.bots.rl.v2.learning.cardaction.AbstractCardActionTrainer;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.decisions.CardActionSelectionDecision;

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
            protected double[] getCardVector(LotroGame game, int cardId, String blueprintId, String playerName) throws CardNotFoundException {
                if (actionStartsWith.equals("Play")) {
                    return BotService.staticLibrary.getLotroCardBlueprint(blueprintId).getSpecificPlayFromHandCardFeatures(game, cardId, playerName);
                } else {
                    return super.getCardVector(game, cardId, blueprintId, playerName);
                }
            }
        };
    }

    public  static AbstractCardActionTrainer makeAndRegisterTrainer(String phaseTrigger, String actionStartsWith, String blueprintId) {
        AbstractCardActionTrainer trainer = makeTrainer(phaseTrigger, actionStartsWith, blueprintId);
        TrainersV2.addSubTrainer(trainer);

        return trainer;
    }
}
