package com.gempukku.lotro.cards.build.bot.ability2;

import com.gempukku.lotro.cards.build.bot.abstractcard.BotCard;
import com.gempukku.lotro.game.state.PlannedBoardState;

public interface Ability {
    void resolveAbility(BotCard source, PlannedBoardState plannedBoardState);
    boolean canPayCost(BotCard source, PlannedBoardState plannedBoardState);
    double getValueIfUsed(BotCard source, PlannedBoardState plannedBoardState);
    boolean conditionOk(BotCard source, PlannedBoardState plannedBoardState);
}
