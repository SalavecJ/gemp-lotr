package com.gempukku.lotro.bots.v2.simulation;

import com.gempukku.lotro.bots.v2.decision.option.AssignMinionOption;
import com.gempukku.lotro.bots.v2.decision.option.Option;
import com.gempukku.lotro.bots.v2.decision.request.AssignMinionsDecisionRequest;
import com.gempukku.lotro.bots.v2.decision.request.DecisionRequest;
import com.gempukku.lotro.bots.v2.decision.request.DecisionRequestFactory;
import com.gempukku.lotro.game.DefaultUserFeedback;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.List;

import static com.gempukku.lotro.bots.v2.fotr_starter.utility.OptionFinders.*;

public class SimulatedGame {

    private final DefaultLotroGame copy;
    private final DefaultUserFeedback userFeedback = new DefaultUserFeedback();

    public SimulatedGame(DefaultLotroGame game) {
        this.copy = game.getCopyByReplayingDecisionsFromStart(userFeedback);
    }

    public DecisionRequest getDecisionRequest(String player) {
        AwaitingDecision awaitingDecision = userFeedback.getAwaitingDecision(player);
        if (awaitingDecision == null) {
            return null;
        }
        return DecisionRequestFactory.create(copy, awaitingDecision);
    }

    public void makeDecision(String player, String answer) {
        AwaitingDecision awaitingDecision = userFeedback.getAwaitingDecision(player);
        userFeedback.participantDecided(player, answer);
        try {
            awaitingDecision.decisionMade(answer);
        } catch (DecisionResultInvalidException e) {
            throw new IllegalStateException("Chosen action was invalid: " + answer, e);
        }
        copy.carryOutPendingActionsUntilDecisionNeeded();
    }

    public void assignAndMoveToSkirmishFpPlayer(PhysicalCard companion, PhysicalCard minion) {
        String player = companion.getOwner();
        DecisionRequest fpAssignmentDecisionRequest = getDecisionRequest(player);
        if (!(fpAssignmentDecisionRequest instanceof AssignMinionsDecisionRequest assignMinionsDecisionRequest)) {
            throw new IllegalStateException("Player " + player + " cannot assign minions now.");
        }
        makeDecision(player, fpAssignmentDecisionRequest.combineSelected(assignTogether(assignMinionsDecisionRequest, companion, minion)));

        DecisionRequest chooseSkirmish = getDecisionRequest(player);
        if (chooseSkirmish == null) {
            // shadow assignment
            DecisionRequest shadowAssignmentDecisionRequest = getDecisionRequest(getOpponent(player));
            makeDecision(getOpponent(player), shadowAssignmentDecisionRequest.combineSelected(assignNotTo((AssignMinionsDecisionRequest) shadowAssignmentDecisionRequest, companion)));
            // let fp player choose skirmish
            chooseSkirmish = getDecisionRequest(player);
        }

        if (chooseSkirmish == null || !chooseSkirmish.isNextSkirmishDecision()) {
            throw new IllegalStateException("Player " + player + " cannot choose skirmish now.");
        }

        // choose skirmish with the companion
        makeDecision(player, findNextSkirmish(chooseSkirmish.getOptions(), companion).getAnswerForGame());

        // minion assigned to companion, skirmish has started
    }

    public DefaultLotroGame getGame() {
        return copy;
    }

    public SimulatedGame copy() {
        return new SimulatedGame(copy);
    }

    public String getOpponent(String player) {
        return copy.getGameState().getOpponent(player);
    }

    private List<Option> assignTogether(AssignMinionsDecisionRequest assignMinionsDecisionRequest, PhysicalCard companion, PhysicalCard minion) {
        while (assignMinionsDecisionRequest.getNextMinion() != null) {
            if (assignMinionsDecisionRequest.getOptions().size() == 1) {
                // only one option, assign this minion to the companion
                assignMinionsDecisionRequest.assign((AssignMinionOption) assignMinionsDecisionRequest.getOptions().getFirst());
            } else if (assignMinionsDecisionRequest.getNextMinion().getCardId() == minion.getCardId()) {
                // assign this minion to the companion
                AssignMinionOption selectedOption = findAssignToFpCharacter(assignMinionsDecisionRequest.getOptions(), companion);
                if (selectedOption == null) {
                    throw new IllegalStateException("Cannot find option to assign minion " + minion.getBlueprint().getFullName() + " to companion " + companion.getBlueprint().getFullName() + " in decision request: " + assignMinionsDecisionRequest);
                }
                assignMinionsDecisionRequest.assign(selectedOption);
            } else {
                // don't assign
                AssignMinionOption selectedOption = findDoNotAssignOption(assignMinionsDecisionRequest.getOptions());
                if (selectedOption == null) {
                    throw new IllegalStateException("Cannot find option to assign minion " + assignMinionsDecisionRequest.getNextMinion().getBlueprint().getFullName() + " to another companion than " + companion.getBlueprint().getFullName() + " in decision request: " + assignMinionsDecisionRequest);
                }
                assignMinionsDecisionRequest.assign(selectedOption);
            }
        }
        return assignMinionsDecisionRequest.getChosenOptions();
    }

    private List<Option> assignNotTo(AssignMinionsDecisionRequest assignMinionsDecisionRequest, PhysicalCard companion) {
        while (assignMinionsDecisionRequest.getNextMinion() != null) {
            if (assignMinionsDecisionRequest.getOptions().size() == 1) {
                // only one option, assign this minion to the companion
               assignMinionsDecisionRequest.assign((AssignMinionOption) assignMinionsDecisionRequest.getOptions().getFirst());
            } else {
                // assign this minion to another companion
                AssignMinionOption selectedOption = findAssignToOtherFpCharacter(assignMinionsDecisionRequest.getOptions(), companion);
                if (selectedOption == null) {
                    throw new IllegalStateException("Cannot find option to assign minion " + assignMinionsDecisionRequest.getNextMinion().getBlueprint().getFullName() + " to another companion than " + companion.getBlueprint().getFullName() + " in decision request: " + assignMinionsDecisionRequest);
                }
                assignMinionsDecisionRequest.assign(selectedOption);
            }
        }
        return assignMinionsDecisionRequest.getChosenOptions();
    }

}
