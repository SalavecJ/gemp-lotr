package com.gempukku.lotro.bots.v2.fotr_starter.skirmish;

import com.gempukku.lotro.bots.v2.decision.option.Option;
import com.gempukku.lotro.bots.v2.decision.option.PassOption;
import com.gempukku.lotro.bots.v2.decision.request.DecisionRequest;
import com.gempukku.lotro.bots.v2.simulation.SimulatedGame;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.state.Skirmish;

import java.util.ArrayList;

import static com.gempukku.lotro.bots.v2.BotLogging.LogLevel.REASONING;
import static com.gempukku.lotro.bots.v2.fotr_starter.utility.GameQueries.*;
import static com.gempukku.lotro.bots.v2.fotr_starter.utility.OptionFinders.findOptionOfType;

public class SkirmishPrediction {
    private final SimulatedGame startOfSkirmish;

    private final String fpPlayer;
    private final String shadowPlayer;

    private final SimulatedGame passAllSimulation;

    public SkirmishPrediction(SimulatedGame simulation) {
        this.startOfSkirmish = simulation.copy();

        Skirmish skirmish = startOfSkirmish.getGame().getGameState().getSkirmish();

        fpPlayer = skirmish.getFellowshipCharacter().getOwner();
        shadowPlayer = new ArrayList<>(skirmish.getShadowCharacters()).getFirst().getOwner();

        // FP options
        passAllSimulation = startOfSkirmish.copy();
        DecisionRequest fpDecision = passAllSimulation.getDecisionRequest(fpPlayer);
//        BotLogging.log(REASONING, "Fp Skirmish decision: " + fpDecision);

        // FP passes
        passAllSimulation.makeDecision(fpPlayer, new PassOption().getAnswerForGame());

        // Shadow options
//        BotLogging.log(REASONING, "Shadow Skirmish decision: " + passAllSimulation.getDecisionRequest(shadowPlayer));
        passSkirmish(passAllSimulation);
    }

    public SkirmishResult getResultIfAllPass() {
        return new SkirmishResult(startOfSkirmish, passAllSimulation);
    }


    private void passSkirmish(SimulatedGame simulation) {
        DecisionRequest decision = simulation.getDecisionRequest(fpPlayer);
        DecisionRequest opponentDecision = simulation.getDecisionRequest(shadowPlayer);
        while (true) {
            if (decision == null && opponentDecision == null) {
                break;
            }
            if (simulation.getGame().getGameState().getCurrentPhase() == Phase.REGROUP) {
                break;
            }
            if (decision != null && decision.isNextSkirmishDecision()) {
                break;
            }

            if (decision != null) {
                Option option = findOptionOfType(decision.getOptions(), PassOption.class);
                if (option == null) {
                    if (decision.isRequiredResponsesDecision()) {
                        // whatever order
                        option = decision.getOptions().getFirst();
                    } else {
                        throw new IllegalStateException("Cannot find how to pass through this decision request: " + decision);
                    }
                }
                simulation.makeDecision(fpPlayer, option.getAnswerForGame());
            } else {
                Option option = findOptionOfType(opponentDecision.getOptions(), PassOption.class);
                if (option == null) {
                    throw new IllegalStateException("Cannot find pass option in decision request: " + opponentDecision);
                }
                simulation.makeDecision(shadowPlayer, option.getAnswerForGame());
            }

            decision = simulation.getDecisionRequest(fpPlayer);
            opponentDecision = simulation.getDecisionRequest(shadowPlayer);
        }
    }
}
