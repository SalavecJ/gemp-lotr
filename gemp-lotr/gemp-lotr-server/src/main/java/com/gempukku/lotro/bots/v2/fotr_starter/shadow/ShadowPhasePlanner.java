package com.gempukku.lotro.bots.v2.fotr_starter.shadow;

import com.gempukku.lotro.bots.v2.BotLogging;
import com.gempukku.lotro.bots.v2.fotr_starter.DeckRecognizer;
import com.gempukku.lotro.bots.v2.fotr_starter.Plan;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import static com.gempukku.lotro.bots.v2.BotLogging.LogLevel.DEBUG;

public class ShadowPhasePlanner {
    public static Plan getPlan(DefaultLotroGame game, String player) {
        PhysicalCard currentSite = game.getGameState().getCurrentSite();
        BotLogging.log(DEBUG, "Shadow phase planning for site " + currentSite.getBlueprint().getSiteNumber() + ": " + currentSite.getBlueprint().getFullName());
        return switch (DeckRecognizer.recognize(game.getGameState().getLotroDeck(player))) {
            case ARAGORN_STARTER -> AragornShadowPhasePlanner.getPlan(game, player);
            case GANDALF_STARTER -> GandalfShadowPhasePlanner.getPlan(game, player);
        };
    }
}
