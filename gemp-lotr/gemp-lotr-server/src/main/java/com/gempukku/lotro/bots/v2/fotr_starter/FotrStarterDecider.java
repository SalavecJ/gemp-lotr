package com.gempukku.lotro.bots.v2.fotr_starter;

import com.gempukku.lotro.bots.v2.BotLogging;
import com.gempukku.lotro.bots.v2.decision.Decider;
import com.gempukku.lotro.bots.v2.decision.option.*;
import com.gempukku.lotro.bots.v2.decision.request.AssignMinionsDecisionRequest;
import com.gempukku.lotro.bots.v2.decision.request.DecisionRequest;
import com.gempukku.lotro.bots.v2.fotr_starter.between_turns.MulliganDecider;
import com.gempukku.lotro.bots.v2.fotr_starter.between_turns.SanctuaryPlanner;
import com.gempukku.lotro.bots.v2.fotr_starter.pregame.BurdenBidder;
import com.gempukku.lotro.bots.v2.fotr_starter.pregame.StartingCompanionChooser;
import com.gempukku.lotro.bots.v2.fotr_starter.pregame.StartingPositionChooser;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.ArrayList;
import java.util.List;

public class FotrStarterDecider implements Decider {
    private Plan currentPlan = null;

    @Override
    public List<Option> decide(DecisionRequest decisionRequest, DefaultLotroGame game, String player) {
        int minToSelect = decisionRequest.getMinToSelect();
        int maxToSelect = decisionRequest.getMaxToSelect();

        if (minToSelect > maxToSelect) {
            throw new IllegalArgumentException("minToSelect cannot be greater than maxToSelect");
        } else if (maxToSelect <= 0) {
            throw new IllegalArgumentException("maxToSelect must be greater than 0");
        } else if (minToSelect <= 0) {
            throw new IllegalArgumentException("minToSelect must be greater than 0");
        } else if (decisionRequest.getOptions().size() < minToSelect) {
            throw new IllegalArgumentException("Not enough options to select the minimum required");
        }

        // check plan
        if (currentPlan != null && !currentPlan.isComplete()) {
            List<Option> nextAction = currentPlan.nextAction(decisionRequest);
            BotLogging.log(2, "Choosing planned action");
            return nextAction;
        }

        // pregame decisions
        if (decisionRequest.isBurdenBidDecision()) {
            return List.of(BurdenBidder.bid(decisionRequest, game, player));
        } else if (decisionRequest.isStartingPositionDecision()) {
            return List.of(StartingPositionChooser.choosePosition(decisionRequest));
        } else if (decisionRequest.isStartingCompanionDecision()) {
            return List.of(StartingCompanionChooser.chooseStartingCompanion(decisionRequest, game, player));
        }

        // between turns
        if (decisionRequest.isMulliganDecision()) {
            return List.of(MulliganDecider.decide(decisionRequest, game, player));
        } else if (decisionRequest.isSanctuaryHealDecision()) {
            currentPlan = SanctuaryPlanner.getPlan(decisionRequest, game, player);
            BotLogging.log(2, "Sanctuary plan:\n" + currentPlan);
            return decide(decisionRequest, game, player);
        }

        if (decisionRequest instanceof AssignMinionsDecisionRequest assignMinionsDecisionRequest) {
            return randomlyDecideAssignment(assignMinionsDecisionRequest);
        }

        if (minToSelect == 1 && maxToSelect == 1) {
            int randomIndex = (int) (Math.random() * decisionRequest.getOptions().size());
            return List.of(decisionRequest.getOptions().get(randomIndex));
        } else {
            int realMaxToSelect = Math.min(maxToSelect, decisionRequest.getOptions().size());
            int optionsToSelect = minToSelect + (int) (Math.random() * (realMaxToSelect - minToSelect + 1));

            List<Integer> selectedIndices = new ArrayList<>();
            while (selectedIndices.size() < optionsToSelect) {
                int randomIndex = (int) (Math.random() * decisionRequest.getOptions().size());
                if (!selectedIndices.contains(randomIndex)) {
                    selectedIndices.add(randomIndex);
                }
            }

            List<Option> selectedOptions = new ArrayList<>();
            for (int index : selectedIndices) {
                selectedOptions.add(decisionRequest.getOptions().get(index));
            }

            return selectedOptions;
        }
    }

    @Override
    public void cleanUpAfterGame() {
        currentPlan = null;
    }

    private List<Option> randomlyDecideAssignment(AssignMinionsDecisionRequest assignMinionsDecisionRequest) {
        while (assignMinionsDecisionRequest.getNextMinion() != null) {
            int randomIndex = (int) (Math.random() * assignMinionsDecisionRequest.getOptions().size());
            AssignMinionOption selectedOption = ((AssignMinionOption) assignMinionsDecisionRequest.getOptions().get(randomIndex));
            assignMinionsDecisionRequest.assign(selectedOption);
        }
        return assignMinionsDecisionRequest.getChosenOptions();
    }
}
