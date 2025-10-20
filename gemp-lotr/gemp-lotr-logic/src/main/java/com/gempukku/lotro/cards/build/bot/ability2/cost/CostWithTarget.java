package com.gempukku.lotro.cards.build.bot.ability2.cost;

import com.gempukku.lotro.cards.build.bot.abstractcard.BotCard;
import com.gempukku.lotro.game.state.PlannedBoardState;

import java.util.ArrayList;

public abstract class CostWithTarget extends Cost {
    public abstract ArrayList<BotCard> getPotentialTargets(PlannedBoardState plannedBoardState);
    public abstract BotCard chooseTarget(PlannedBoardState plannedBoardState);
}
