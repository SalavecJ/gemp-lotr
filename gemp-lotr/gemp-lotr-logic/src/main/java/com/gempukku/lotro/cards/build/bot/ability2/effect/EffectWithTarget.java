package com.gempukku.lotro.cards.build.bot.ability2.effect;

import com.gempukku.lotro.cards.build.bot.abstractcard.BotCard;
import com.gempukku.lotro.game.state.PlannedBoardState;

import java.util.ArrayList;
import java.util.List;

public abstract class EffectWithTarget extends Effect{
    public abstract ArrayList<BotCard> getPotentialTargets(String player, PlannedBoardState plannedBoardState);
    public abstract boolean affectsAll();
    public abstract BotCard chooseTarget(String player, PlannedBoardState plannedBoardState);
    public abstract String toString(String player, PlannedBoardState plannedBoardState, List<BotCard> targets);
    public String toString(String player, PlannedBoardState plannedBoardState) {
        throw new IllegalStateException("Cannot produce string without targets provided");
    }
}
