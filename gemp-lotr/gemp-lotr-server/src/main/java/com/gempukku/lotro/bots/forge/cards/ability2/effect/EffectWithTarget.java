package com.gempukku.lotro.bots.forge.cards.ability2.effect;

import com.gempukku.lotro.bots.forge.cards.abstractcard.BotCard;
import com.gempukku.lotro.bots.forge.plan.PlannedBoardState;

import java.util.ArrayList;
import java.util.List;

public abstract class EffectWithTarget extends Effect{
    public abstract void resolveWithTarget(String player, PlannedBoardState plannedBoardState, BotCard target);
    public abstract double getValueIfResolvedWithTarget(String player, PlannedBoardState plannedBoardState, BotCard target);

    public abstract ArrayList<BotCard> getPotentialTargets(String player, PlannedBoardState plannedBoardState);
    public abstract boolean affectsAll();
    public abstract BotCard chooseTarget(String player, PlannedBoardState plannedBoardState);
    public abstract String toString(String player, PlannedBoardState plannedBoardState, List<BotCard> targets);
    public String toString(String player, PlannedBoardState plannedBoardState) {
        throw new IllegalStateException("Cannot produce string without targets provided");
    }
}
