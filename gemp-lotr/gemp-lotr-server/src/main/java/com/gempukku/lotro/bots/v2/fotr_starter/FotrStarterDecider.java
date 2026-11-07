package com.gempukku.lotro.bots.v2.fotr_starter;

import com.gempukku.lotro.bots.v2.BotLogging;
import com.gempukku.lotro.bots.v2.decision.Decider;
import com.gempukku.lotro.bots.v2.decision.option.*;
import com.gempukku.lotro.bots.v2.decision.request.AssignMinionsDecisionRequest;
import com.gempukku.lotro.bots.v2.decision.request.DecisionRequest;
import com.gempukku.lotro.bots.v2.fotr_starter.between_turns.AragornTriggerAcceptor;
import com.gempukku.lotro.bots.v2.fotr_starter.between_turns.MulliganDecider;
import com.gempukku.lotro.bots.v2.fotr_starter.between_turns.SanctuaryPlanner;
import com.gempukku.lotro.bots.v2.fotr_starter.fellowship.FellowshipPhasePlanner;
import com.gempukku.lotro.bots.v2.fotr_starter.maneuver.ManeuverPhasePlanner;
import com.gempukku.lotro.bots.v2.fotr_starter.pregame.BurdenBidder;
import com.gempukku.lotro.bots.v2.fotr_starter.pregame.StartingCompanionChooser;
import com.gempukku.lotro.bots.v2.fotr_starter.pregame.StartingPositionChooser;
import com.gempukku.lotro.bots.v2.fotr_starter.shadow.ShadowPhasePlanner;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.ArrayList;
import java.util.List;

import static com.gempukku.lotro.bots.v2.BotLogging.LogLevel.DEBUG;

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
            BotLogging.log(DEBUG, "Choosing planned action");
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
            return decide(decisionRequest, game, player);
        } else if (game.getGameState().getCurrentPhase() == Phase.BETWEEN_TURNS && decisionRequest.isOptionalResponsesDecision()) {
            return List.of(AragornTriggerAcceptor.decide(decisionRequest, game, player));
        }

        // fellowship phase
        if (decisionRequest.isFellowshipPhaseDecision()) {
            currentPlan = FellowshipPhasePlanner.getPlan(game, player);
            return decide(decisionRequest, game, player);
        }

        // shadow phase
        if (decisionRequest.isShadowPhaseDecision()) {
            currentPlan = ShadowPhasePlanner.getPlan(game, player);
            return decide(decisionRequest, game, player);
        }

        // maneuver phase
        if (decisionRequest.isManeuverPhaseDecision()) {
            currentPlan = ManeuverPhasePlanner.getPlan(decisionRequest, game, player);
            return decide(decisionRequest, game, player);
        }

        BotLogging.log(DEBUG, "Choosing action randomly");

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
