package com.gempukku.lotro.bots.v2.fotr_starter.pregame;

import com.gempukku.lotro.bots.v2.BotLogging;
import com.gempukku.lotro.bots.v2.decision.option.BurdenBidOption;
import com.gempukku.lotro.bots.v2.decision.option.Option;
import com.gempukku.lotro.bots.v2.decision.request.DecisionRequest;
import com.gempukku.lotro.bots.v2.fotr_starter.DeckRecognizer;
import com.gempukku.lotro.bots.v2.fotr_starter.FotrStarterDeck;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.List;

public class BurdenBidder {

    public static Option bid(DecisionRequest decisionRequest, DefaultLotroGame game, String player) {
        if (decisionRequest.getOptions().stream().anyMatch(option -> !(option instanceof BurdenBidOption))) {
            throw new IllegalArgumentException("Unexpected option type in decision request: " + decisionRequest);
        }

        int minToBid = 0;
        FotrStarterDeck deck = DeckRecognizer.recognize(game.getGameState().getLotroDeck(player));
        boolean isPlayingAragornDeck = deck == FotrStarterDeck.ARAGORN_STARTER;
        int max = isPlayingAragornDeck ? 5 : 4;

        List<BurdenBidOption> optionList = decisionRequest.getOptions().stream()
                .filter(option -> option instanceof BurdenBidOption)
                .map(option -> (BurdenBidOption) option)
                .filter(option -> option.getBid() >= minToBid && option.getBid() <= max)
                .toList();

        if (optionList.isEmpty()) {
            throw new IllegalArgumentException("No valid options to bid burdens: " + decisionRequest);
        }

        int randomIndex = (int) (Math.random() * optionList.size());
        BotLogging.log(2, "Maximum burdens to bid " + max + " for " + deck);
        BotLogging.log(2, "Random bid in range: " + optionList.get(randomIndex).getBid());
        return optionList.get(randomIndex);
    }
}
