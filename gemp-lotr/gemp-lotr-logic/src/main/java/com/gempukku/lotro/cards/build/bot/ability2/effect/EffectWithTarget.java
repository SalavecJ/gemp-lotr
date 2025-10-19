package com.gempukku.lotro.cards.build.bot.ability2.effect;

import com.gempukku.lotro.cards.build.bot.abstractcard.BotCard;
import com.gempukku.lotro.game.state.PlannedBoardState;

import java.util.List;

public abstract class EffectWithTarget extends Effect{
    public abstract List<BotCard> getPotentialTargets(BotCard source, PlannedBoardState plannedBoardState);
    public abstract boolean affectsAll();
    public abstract BotCard chooseTarget(BotCard source, PlannedBoardState plannedBoardState);
}
