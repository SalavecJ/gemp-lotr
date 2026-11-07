package com.gempukku.lotro.bots.v2.fotr_starter.archery;

import com.gempukku.lotro.bots.v2.BotLogging;
import com.gempukku.lotro.bots.v2.decision.option.PassOption;
import com.gempukku.lotro.bots.v2.decision.request.DecisionRequest;
import com.gempukku.lotro.bots.v2.fotr_starter.Plan;
import com.gempukku.lotro.bots.v2.fotr_starter.SimplePlan;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.List;

import static com.gempukku.lotro.bots.v2.BotLogging.LogLevel.DEBUG;
import static com.gempukku.lotro.bots.v2.BotLogging.LogLevel.REASONING;

public class ArcheryPhasePlanner {
    public static Plan getPlan(DecisionRequest decisionRequest, DefaultLotroGame game, String player) {
        PhysicalCard currentSite = game.getGameState().getCurrentSite();
        boolean fpPlayer = game.getGameState().getCurrentPlayerId().equals(player);

        if (decisionRequest.isArcheryPhaseDecision()) {
            BotLogging.log(DEBUG, "Archery phase " + (fpPlayer ? "FP" : "Shadow") + " planning for site " + currentSite.getBlueprint().getSiteNumber() + ": " + currentSite.getBlueprint().getFullName());
            BotLogging.log(REASONING, "Nothing to be done");
            Plan passPlan = getPassPlan();
            BotLogging.log(DEBUG, "Archery phase plan: Pass\n" + passPlan);
            return passPlan;
        }

        BotLogging.log(DEBUG, "Archery " + (fpPlayer ? "FP" : "Shadow") + " wounds planning for site " + currentSite.getBlueprint().getSiteNumber() + ": " + currentSite.getBlueprint().getFullName());

        return fpPlayer ?
                FpArcheryWoundsPlanner.getPlan(decisionRequest, game, player) :
                ShadowArcheryWoundsPlanner.getPlan(decisionRequest, game, player);
    }



    private static SimplePlan getPassPlan() {
        return new SimplePlan(List.of(List.of(new PassOption())));
    }
}
