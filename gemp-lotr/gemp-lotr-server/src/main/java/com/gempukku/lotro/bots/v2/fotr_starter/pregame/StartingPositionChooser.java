package com.gempukku.lotro.bots.v2.fotr_starter.pregame;

import com.gempukku.lotro.bots.v2.BotLogging;
import com.gempukku.lotro.bots.v2.decision.option.Option;
import com.gempukku.lotro.bots.v2.decision.option.StartingPositionOption;
import com.gempukku.lotro.bots.v2.decision.request.DecisionRequest;

public class StartingPositionChooser {

    public static Option choosePosition(DecisionRequest decisionRequest) {
        if (decisionRequest.getOptions().stream().anyMatch(option -> !(option instanceof StartingPositionOption))) {
            throw new IllegalArgumentException("Unexpected option type in decision request: " + decisionRequest);
        }

        for (Option option : decisionRequest.getOptions()) {
            StartingPositionOption startingPositionOption = (StartingPositionOption) option;
            if (startingPositionOption.getStartingPosition() == 1) {
                BotLogging.log(2, "Always choosing to go first");
                return startingPositionOption;
            }
        }

        throw new IllegalArgumentException("No option to go first found in decision request: " + decisionRequest);
    }
}
