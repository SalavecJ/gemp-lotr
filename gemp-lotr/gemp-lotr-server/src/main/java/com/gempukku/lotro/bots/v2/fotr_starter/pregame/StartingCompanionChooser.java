package com.gempukku.lotro.bots.v2.fotr_starter.pregame;

import com.gempukku.lotro.bots.v2.BotLogging;
import com.gempukku.lotro.bots.v2.decision.option.Option;
import com.gempukku.lotro.bots.v2.decision.option.PassOption;
import com.gempukku.lotro.bots.v2.decision.option.StartingCompanionOption;
import com.gempukku.lotro.bots.v2.decision.request.DecisionRequest;
import com.gempukku.lotro.bots.v2.fotr_starter.DeckRecognizer;
import com.gempukku.lotro.bots.v2.fotr_starter.FotrStarterDeck;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.List;

import static com.gempukku.lotro.bots.v2.BotLogging.LogLevel.DEBUG;

public class StartingCompanionChooser {

    public static Option chooseStartingCompanion(DecisionRequest decisionRequest, DefaultLotroGame game, String player) {
        if (decisionRequest.getOptions().stream().anyMatch(option -> !(option instanceof StartingCompanionOption) && !(option instanceof PassOption))) {
            throw new IllegalArgumentException("Unexpected option type in decision request: " + decisionRequest);
        }

        FotrStarterDeck deck = DeckRecognizer.recognize(game.getGameState().getLotroDeck(player));
        boolean isPlayingAragornDeck = deck == FotrStarterDeck.ARAGORN_STARTER;
        boolean isPlayingGandalfDeck = deck == FotrStarterDeck.GANDALF_STARTER;

        List<StartingCompanionOption> startingCompanionOptions = decisionRequest.getOptions().stream()
                .filter(option -> option instanceof StartingCompanionOption)
                .map(option -> (StartingCompanionOption) option)
                .toList();

        if (startingCompanionOptions.isEmpty()) {
            return new PassOption();
        }

        for (StartingCompanionOption startingCompanionOption : startingCompanionOptions) {
            if (startingCompanionOption.getBlueprint().getFullName().contains("Aragorn") && isPlayingAragornDeck) {
                BotLogging.log(DEBUG, deck + " starts with " + startingCompanionOption.getBlueprint().getFullName());
                return startingCompanionOption;
            }
            if (startingCompanionOption.getBlueprint().getFullName().contains("Gandalf") && isPlayingGandalfDeck) {
                BotLogging.log(DEBUG, deck + " starts with " + startingCompanionOption.getBlueprint().getFullName());
                return startingCompanionOption;
            }
        }

        throw new IllegalStateException("No suitable starting companion found for the deck being played. Decision request: " + decisionRequest);
    }
}
