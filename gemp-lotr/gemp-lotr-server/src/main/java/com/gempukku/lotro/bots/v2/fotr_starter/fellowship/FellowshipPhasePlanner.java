package com.gempukku.lotro.bots.v2.fotr_starter.fellowship;

import com.gempukku.lotro.bots.v2.fotr_starter.DeckRecognizer;
import com.gempukku.lotro.bots.v2.fotr_starter.Plan;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

public class FellowshipPhasePlanner {
    public static Plan getPlan(DefaultLotroGame game, String player) {
        return switch (DeckRecognizer.recognize(game.getGameState().getLotroDeck(player))) {
            case ARAGORN_STARTER -> AragornFellowshipPhasePlanner.getPlan(game, player);
            case GANDALF_STARTER -> GandalfFellowshipPhasePlanner.getPlan(game, player);
        };
    }
}
