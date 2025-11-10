package com.gempukku.lotro.bots.forge.cards.ability2.effect;

import com.gempukku.lotro.bots.forge.cards.abstractcard.BotCard;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.bots.forge.plan.PlannedBoardState;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class EffectPlayFromDiscard extends EffectWithTarget {
    private final Predicate<BotCard> targetPredicate;

    public EffectPlayFromDiscard(Predicate<BotCard> targetPredicate) {
        this.targetPredicate = targetPredicate;
    }

    @Override
    public ArrayList<BotCard> getPotentialTargets(String player, PlannedBoardState plannedBoardState) {
        return new ArrayList<>(plannedBoardState.getDiscard(player).stream()
                .filter(targetPredicate)
                .filter(botCard -> botCard.canBePlayed(plannedBoardState))
                .filter(botCard -> {
                    boolean roaming = false;
                    if (botCard.getSelf().getBlueprint().getCardType() == CardType.MINION) {
                        int currentSiteNumber = plannedBoardState.getCurrentSite().getSelf().getBlueprint().getSiteNumber();
                        int minionSiteNumber = botCard.getSelf().getBlueprint().getSiteNumber();
                        roaming = minionSiteNumber > currentSiteNumber;
                    }
                    return botCard.getSelf().getBlueprint().getSide() == Side.FREE_PEOPLE
                            || plannedBoardState.getTwilight() >= botCard.getSelf().getBlueprint().getTwilightCost() + (roaming ? 2 : 0);
                })
                .toList());
    }

    @Override
    public boolean affectsAll() {
        return false;
    }

    @Override
    public void resolveWithTarget(String player, PlannedBoardState plannedBoardState, BotCard target) {
        plannedBoardState.playCardFromDiscard(target);
    }

    @Override
    public double getValueIfResolvedWithTarget(String player, PlannedBoardState plannedBoardState, BotCard target) {
        return 0;
    }

    @Override
    public BotCard chooseTarget(String player, PlannedBoardState plannedBoardState) {
        throw new IllegalStateException("Cannot choose target automatically for EffectFromDiscard");
    }

    @Override
    public void resolve(String player, PlannedBoardState plannedBoardState) {
        throw new IllegalStateException("Cannot choose target automatically for EffectFromDiscard");
    }

    @Override
    public double getValueIfResolved(String player, PlannedBoardState plannedBoardState) {
        throw new IllegalStateException("Cannot choose target automatically for EffectFromDiscard");
    }

    @Override
    public String toString(String player, PlannedBoardState plannedBoardState, List<BotCard> targets) {
        if (targets.isEmpty()) {
            return "attempt to play card from discard, but none can be chosen";
        } else if (targets.size() == 1) {
            return "play " + targets.getFirst().getSelf().getBlueprint().getFullName() + " from discard";
        } else {
            throw new IllegalStateException("EffectFromDiscard cannot be applied to multiple targets");
        }
    }
}
