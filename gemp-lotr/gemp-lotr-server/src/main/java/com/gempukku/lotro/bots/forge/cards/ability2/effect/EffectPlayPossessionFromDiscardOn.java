package com.gempukku.lotro.bots.forge.cards.ability2.effect;

import com.gempukku.lotro.bots.forge.cards.abstractcard.BotCard;
import com.gempukku.lotro.bots.forge.cards.abstractcard.BotObjectAttachableCard;
import com.gempukku.lotro.bots.forge.plan.PlannedBoardState;
import com.gempukku.lotro.common.CardType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class EffectPlayPossessionFromDiscardOn extends EffectWithTarget {
    private final Predicate<BotCard> possessionPredicate;
    private final Predicate<BotCard> bearerPredicate;

    public EffectPlayPossessionFromDiscardOn(Predicate<BotCard> possessionPredicate, Predicate<BotCard> bearerPredicate) {
        this.possessionPredicate = possessionPredicate;
        this.bearerPredicate = bearerPredicate;
    }

    @Override
    public ArrayList<BotCard> getPotentialTargets(String player, PlannedBoardState plannedBoardState) {
        return new ArrayList<>(plannedBoardState.getDiscard(player).stream()
                .filter(botCard -> botCard.getSelf().getBlueprint().getCardType() == CardType.POSSESSION && possessionPredicate.test(botCard))
                .filter(botCard -> botCard instanceof BotObjectAttachableCard)
                .filter(botCard -> botCard.canBePlayed(plannedBoardState))
                .filter(botCard -> plannedBoardState.getTwilight() >= botCard.getSelf().getBlueprint().getTwilightCost())
                .filter(botCard -> plannedBoardState.getActiveCards().stream().anyMatch(activeCard -> {
                    BotObjectAttachableCard attachableCard = (BotObjectAttachableCard) botCard;
                    return attachableCard.isValidBearer(activeCard, plannedBoardState)
                            && bearerPredicate.test(activeCard);
                }))
                .toList());
    }

    @Override
    public boolean affectsAll() {
        return false;
    }

    @Override
    public void resolveOn(String player, PlannedBoardState plannedBoardState, BotCard target) {
        plannedBoardState.playPossessionFromDiscardOn(target, bearerPredicate);
    }

    @Override
    protected double getValueIfResolvedOn(String player, PlannedBoardState plannedBoardState, BotCard target) {
        return 0.1; // Playing a card from discard has some value, but hard to quantify
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
