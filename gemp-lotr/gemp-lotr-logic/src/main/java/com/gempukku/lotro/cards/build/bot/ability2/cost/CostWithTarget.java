package com.gempukku.lotro.cards.build.bot.ability2.cost;

import com.gempukku.lotro.cards.build.bot.abstractcard.BotCard;
import com.gempukku.lotro.game.state.PlannedBoardState;

import java.util.List;

public abstract class CostWithTarget extends Cost {
    public abstract List<BotCard> getPotentialTargets(PlannedBoardState plannedBoardState);
    public abstract BotCard chooseTarget(PlannedBoardState plannedBoardState);
}
