package com.gempukku.lotro.bots.rl.v2.learning.cardaction.general;

import com.gempukku.lotro.bots.BotService;
import com.gempukku.lotro.bots.rl.v2.learning.cardaction.AbstractCardActionTrainer;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.decisions.CardActionSelectionDecision;

import java.util.Arrays;
import java.util.List;

public class GeneralCardActionTrainerFactory {
    public static List<AbstractCardActionTrainer> getGeneralTrainers(String phaseTrigger) {
        return List.of(
                new AbstractCardActionTrainer() {
                    @Override
                    protected String getTextTrigger() {
                        return phaseTrigger;
                    }

                    @Override
                    protected boolean containsRelevantDecision(GameState gameState, CardActionSelectionDecision decision, String playerName) {
                        return Arrays.stream(decision.getDecisionParameters().get("actionText")).anyMatch(s -> s.startsWith("Play"));
                    }

                    @Override
                    protected boolean relevantCardChosen(GameState gameState, CardActionSelectionDecision decision, String answer) {
                        return decision.getDecisionParameters().get("actionText")[Integer.parseInt(answer)].startsWith("Play");
                    }

                    @Override
                    public String getName() {
                        return "CasGeneralTrainerPlay-" + getTextTrigger();
                    }

                    @Override
                    protected double[] getCardVector(LotroGame game, int cardId, String blueprintId, String playerName) throws CardNotFoundException {
                        return BotService.staticLibrary.getLotroCardBlueprint(blueprintId).getPlayFromHandCardFeatures(game, cardId, playerName);
                    }
                },
                new AbstractCardActionTrainer() {
                    @Override
                    protected String getTextTrigger() {
                        return phaseTrigger;
                    }

                    @Override
                    protected boolean containsRelevantDecision(GameState gameState, CardActionSelectionDecision decision, String playerName) {
                        return Arrays.stream(decision.getDecisionParameters().get("actionText")).anyMatch(s -> s.startsWith("Use"));
                    }

                    @Override
                    protected boolean relevantCardChosen(GameState gameState, CardActionSelectionDecision decision, String answer) {
                        return decision.getDecisionParameters().get("actionText")[Integer.parseInt(answer)].startsWith("Use");
                    }

                    @Override
                    public String getName() {
                        return "CasGeneralTrainerUse-" + getTextTrigger();
                    }
                },
                new AbstractCardActionTrainer() {
                    @Override
                    protected String getTextTrigger() {
                        return phaseTrigger;
                    }

                    @Override
                    protected boolean containsRelevantDecision(GameState gameState, CardActionSelectionDecision decision, String playerName) {
                        return Arrays.stream(decision.getDecisionParameters().get("actionText")).anyMatch(s -> s.startsWith("Heal by discarding"));
                    }

                    @Override
                    protected boolean relevantCardChosen(GameState gameState, CardActionSelectionDecision decision, String answer) {
                        return decision.getDecisionParameters().get("actionText")[Integer.parseInt(answer)].startsWith("Heal by discarding");
                    }

                    @Override
                    public String getName() {
                        return "CasGeneralTrainerHeal-" + getTextTrigger();
                    }
                },
                new AbstractCardActionTrainer() {
                    @Override
                    protected String getTextTrigger() {
                        return phaseTrigger;
                    }

                    @Override
                    protected boolean containsRelevantDecision(GameState gameState, CardActionSelectionDecision decision, String playerName) {
                        return Arrays.stream(decision.getDecisionParameters().get("actionText")).anyMatch(s -> s.startsWith("Transfer"));
                    }

                    @Override
                    protected boolean relevantCardChosen(GameState gameState, CardActionSelectionDecision decision, String answer) {
                        return decision.getDecisionParameters().get("actionText")[Integer.parseInt(answer)].startsWith("Transfer");
                    }

                    @Override
                    public String getName() {
                        return "CasGeneralTrainerTransfer-" + getTextTrigger();
                    }
                });
    }
}
