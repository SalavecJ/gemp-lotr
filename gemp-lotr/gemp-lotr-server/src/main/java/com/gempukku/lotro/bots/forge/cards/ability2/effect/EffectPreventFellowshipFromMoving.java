package com.gempukku.lotro.bots.forge.cards.ability2.effect;

import com.gempukku.lotro.bots.forge.plan.PlannedBoardState;

public class EffectPreventFellowshipFromMoving extends Effect {

    public EffectPreventFellowshipFromMoving() {

    }

    @Override
    public void resolve(String player, PlannedBoardState plannedBoardState) {
        plannedBoardState.preventFellowshipMovement();
    }

    @Override
    public double getValueIfResolved(String player, PlannedBoardState plannedBoardState) {
        if (plannedBoardState.fellowshipCanMove()) {
            return 5;
        } else {
            return 0;
        }
    }

    @Override
    public String toString(String player, PlannedBoardState plannedBoardState) {
        return "prevent fellowship from moving";
    }
}
