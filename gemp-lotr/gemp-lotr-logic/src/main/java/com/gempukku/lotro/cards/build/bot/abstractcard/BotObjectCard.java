package com.gempukku.lotro.cards.build.bot.abstractcard;

import com.gempukku.lotro.cards.build.bot.ability2.condition.Condition;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.PossessionClass;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.PlannedBoardState;

import java.util.Set;

public abstract class BotObjectCard extends BotCard {
    public BotObjectCard(PhysicalCard self) {
        super(self);
    }

    @Override
    public boolean canBePlayedNoMatterThePhase(PlannedBoardState plannedBoardState) {
        if (!uniqueRequirementOk(plannedBoardState)) return false;
        if (!playsToSupportArea()
                && !plannedBoardState.canSpot(self.getOwner(), botCard -> isValidBearer(botCard, plannedBoardState))
                && !isValidPlayableBearerInHand(plannedBoardState))
            return false;
        return (getCondition() == null || getCondition().isOk(this.getSelf().getOwner(), plannedBoardState));
    }

    @Override
    public boolean canBePlayed(PlannedBoardState plannedBoardState) {
        return super.canBePlayed(plannedBoardState)
                && (playsToSupportArea() || plannedBoardState.canSpot(self.getOwner(), botCard -> isValidBearer(botCard, plannedBoardState)));
    }

    /**
     * Hook for subclasses to implement card-specific rules for current board state
     */
    public Condition getCondition() {
        return null;
    }

    /**
     * Hook for subclasses to implement card-specific rules for potential play
     */
    protected abstract boolean canBearThis(PlannedBoardState plannedBoardState, BotCard target);

    protected abstract boolean playsToSupportArea();

    private boolean isValidPlayableBearerInHand(PlannedBoardState plannedBoardState) {
        return plannedBoardState.getHand(self.getOwner()).stream().filter(botCard -> isValidBearer(botCard, plannedBoardState)).anyMatch(botCard -> botCard.canBePlayedNoMatterThePhase(plannedBoardState));
    }

    public boolean isValidBearer(BotCard target, PlannedBoardState plannedBoardState) {
        return canBearThis(plannedBoardState, target) && isCharacter(target) && hasFreeSlotForThis(plannedBoardState, target);
    }

    private boolean isCharacter(BotCard target) {
        CardType ct = target.getSelf().getBlueprint().getCardType();
        return ct.equals(CardType.COMPANION) || ct.equals(CardType.ALLY) || ct.equals(CardType.MINION);
    }

    private boolean hasFreeSlotForThis(PlannedBoardState plannedBoardState, BotCard target) {
        Set<PossessionClass> classes = self.getBlueprint().getPossessionClasses();
        if (classes == null) {
            return true;
        }
        return plannedBoardState.hasFreeSlotForThis(target, classes);
    }
}
