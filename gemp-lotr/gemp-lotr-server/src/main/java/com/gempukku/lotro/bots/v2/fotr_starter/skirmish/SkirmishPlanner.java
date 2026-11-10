package com.gempukku.lotro.bots.v2.fotr_starter.skirmish;

import com.gempukku.lotro.bots.v2.BotLogging;
import com.gempukku.lotro.bots.v2.fotr_starter.Plan;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import static com.gempukku.lotro.bots.v2.BotLogging.LogLevel.DEBUG;

public class SkirmishPlanner {
    public static Plan getPlan(DefaultLotroGame game, String player) {
        PhysicalCard currentSite = game.getGameState().getCurrentSite();
        boolean fpPlayer = game.getGameState().getCurrentPlayerId().equals(player);
        BotLogging.log(DEBUG, "Assignment and Skirmish " + (fpPlayer ? "FP" : "Shadow") + " planning for site " + currentSite.getBlueprint().getSiteNumber() + ": " + currentSite.getBlueprint().getFullName());


        return fpPlayer ? FpSkirmishPlanner.getPlan(game, player) : ShadowSkirmishPlanner.getPlan(game, player);
    }
}
