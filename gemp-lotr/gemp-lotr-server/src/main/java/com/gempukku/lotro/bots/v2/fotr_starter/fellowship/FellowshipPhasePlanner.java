package com.gempukku.lotro.bots.v2.fotr_starter.fellowship;

import com.gempukku.lotro.bots.v2.BotLogging;
import com.gempukku.lotro.bots.v2.fotr_starter.DeckRecognizer;
import com.gempukku.lotro.bots.v2.fotr_starter.Plan;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import static com.gempukku.lotro.bots.v2.BotLogging.LogLevel.DEBUG;

public class FellowshipPhasePlanner {
    public static Plan getPlan(DefaultLotroGame game, String player) {
        PhysicalCard currentSite = game.getGameState().getCurrentSite();
        BotLogging.log(DEBUG, "Fellowship phase planning for site " + currentSite.getBlueprint().getSiteNumber() + ": " + currentSite.getBlueprint().getFullName());
        return switch (DeckRecognizer.recognize(game.getGameState().getLotroDeck(player))) {
            case ARAGORN_STARTER -> AragornFellowshipPhasePlanner.getPlan(game, player);
            case GANDALF_STARTER -> GandalfFellowshipPhasePlanner.getPlan(game, player);
        };
    }
}
