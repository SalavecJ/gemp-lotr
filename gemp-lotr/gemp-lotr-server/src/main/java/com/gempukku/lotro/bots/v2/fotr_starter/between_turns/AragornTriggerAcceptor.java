package com.gempukku.lotro.bots.v2.fotr_starter.between_turns;

import com.gempukku.lotro.bots.v2.BotLogging;
import com.gempukku.lotro.bots.v2.decision.option.ActivateCardOption;
import com.gempukku.lotro.bots.v2.decision.option.Option;
import com.gempukku.lotro.bots.v2.decision.option.PassOption;
import com.gempukku.lotro.bots.v2.decision.request.DecisionRequest;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import static com.gempukku.lotro.bots.v2.BotLogging.LogLevel.DEBUG;

public class AragornTriggerAcceptor {
    private static final String ARAGORN = "Aragorn, King in Exile";
    private static final String SAM = "Sam, Son of Hamfast";

    public static Option decide(DecisionRequest decisionRequest, DefaultLotroGame game, String player) {
        Option activateAragornOption = decisionRequest.getOptions().stream()
                .filter(option -> option instanceof ActivateCardOption activateCardOption && activateCardOption.getCard().getBlueprint().getFullName().equals(ARAGORN))
                .findFirst()
                .orElse(null);
        Option passOption = decisionRequest.getOptions().stream()
                .filter(option -> option instanceof PassOption)
                .findFirst()
                .orElse(null);

        if (activateAragornOption == null) {
            throw new IllegalStateException("No option to activate Aragorn found in decision request: " + decisionRequest);
        }
        if (passOption == null) {
            throw new IllegalStateException("No option to pass found in decision request: " + decisionRequest);
        }

        boolean woundedSamInPlay = game.getGameState().getInPlay().stream()
                .filter(c -> c.getBlueprint().getFullName().equals(SAM))
                .filter(c -> c.getOwner().equals(player))
                .anyMatch(c -> game.getGameState().getWounds(c) > 0);

        if (woundedSamInPlay) {
            BotLogging.log(DEBUG, "Activate Aragorn to heal Sam");
            return activateAragornOption;
        } else {
            BotLogging.log(DEBUG, "Pass - no wounded Sam in play");
            return passOption;
        }
    }
}
