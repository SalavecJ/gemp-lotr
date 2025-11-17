package com.gempukku.lotro.bots.forge.cards.ability2.condition;

import com.gempukku.lotro.bots.forge.plan.PlannedBoardState;

public class ConditionFpCanMove extends Condition {

    public ConditionFpCanMove() {
    }

    @Override
    public boolean isOk(String player, PlannedBoardState plannedBoardState) {
        return plannedBoardState.fellowshipCanMove();
    }
}
