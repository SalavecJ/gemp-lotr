package com.gempukku.lotro.bots.forge.cards.ability2;

import com.gempukku.lotro.bots.forge.cards.abstractcard.BotCard;
import com.gempukku.lotro.bots.forge.plan.PlannedBoardState;

public interface Ability {
    void resolveAbility(String player, PlannedBoardState plannedBoardState);
    void resolveAbilityOnTarget(String player, PlannedBoardState plannedBoardState, BotCard target);
    void resolveAbilityWithCostTarget(String player, PlannedBoardState plannedBoardState, BotCard costTarget);
    void resolveAbilityOnTargetWithCostTarget(String player, PlannedBoardState plannedBoardState, BotCard effectTarget, BotCard costTarget);
    boolean canPayCost(String player, PlannedBoardState plannedBoardState);
    double getValueIfUsed(String player, PlannedBoardState plannedBoardState);
    double getValueIfUsedOnTarget(String player, PlannedBoardState plannedBoardState, BotCard target);
    boolean conditionOk(String player, PlannedBoardState plannedBoardState);
}
