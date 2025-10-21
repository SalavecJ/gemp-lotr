package com.gempukku.lotro.cards.build.bot.ability2;

import com.gempukku.lotro.game.state.PlannedBoardState;

public interface Ability {
    void resolveAbility(String player, PlannedBoardState plannedBoardState);
    boolean canPayCost(String player, PlannedBoardState plannedBoardState);
    double getValueIfUsed(String player, PlannedBoardState plannedBoardState);
    boolean conditionOk(String player, PlannedBoardState plannedBoardState);
}
