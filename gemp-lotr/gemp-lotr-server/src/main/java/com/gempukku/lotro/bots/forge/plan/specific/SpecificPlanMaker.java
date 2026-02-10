package com.gempukku.lotro.bots.forge.plan.specific;

import com.gempukku.lotro.bots.forge.plan.Plan;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

public class SpecificPlanMaker {
    public static boolean canMakePlan(DefaultLotroGame game, AwaitingDecision decision) {
        return MoriaLakePlan.canMakePlan(game, decision);
    }

    public static Plan makePlan(DefaultLotroGame game, AwaitingDecision decision) {
        if (MoriaLakePlan.canMakePlan(game, decision))
            return new MoriaLakePlan(game, decision);
        throw new IllegalArgumentException("Cannot make plan for given decision: " + decision.toJson().toString());
    }
}
