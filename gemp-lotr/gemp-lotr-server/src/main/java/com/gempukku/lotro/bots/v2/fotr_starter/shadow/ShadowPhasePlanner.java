package com.gempukku.lotro.bots.v2.fotr_starter.shadow;

import com.gempukku.lotro.bots.v2.fotr_starter.DeckRecognizer;
import com.gempukku.lotro.bots.v2.fotr_starter.Plan;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

public class ShadowPhasePlanner {
    public static Plan getPlan(DefaultLotroGame game, String player) {
        return switch (DeckRecognizer.recognize(game.getGameState().getLotroDeck(player))) {
            case ARAGORN_STARTER -> AragornShadowPhasePlanner.getPlan(game, player);
            case GANDALF_STARTER -> GandalfShadowPhasePlanner.getPlan(game, player);
        };
    }
}
