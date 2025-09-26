package com.gempukku.lotro.cards.build.bot.abstractcard;

import com.gempukku.lotro.cards.build.bot.RequirementsUtility;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.PossessionClass;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.state.PlannedBoardState;

import java.util.Set;

public abstract class BotObjectCard extends BotCard {
    public BotObjectCard(PhysicalCard self) {
        super(self);
    }

    @Override
    public boolean canBePlayed(PlannedBoardState plannedBoardState) {
        if (!uniqueRequirementOk(plannedBoardState)) return false;
        if (!playsToSupportArea()
                && !plannedBoardState.canSpot(self.getOwner(), botCard -> isValidBearer(botCard, plannedBoardState)))
            return false;
        return otherRequirementsNowOk(plannedBoardState);
    }

    @Override
    public boolean canEverBePlayed() {
        if (!RequirementsUtility.canSpotEver(self.getGame(), self.getOwner(), this::isValidBearer))
            return false;
        return otherRequirementsEverOk(self.getGame());
    }

    /**
     * Hook for subclasses to implement card-specific rules for current board state
     */
    protected boolean otherRequirementsNowOk(PlannedBoardState plannedBoardState) {
        return true;
    }

    /**
     * Hook for subclasses to implement card-specific rules for potential play
     */
    protected boolean otherRequirementsEverOk(LotroGame game) {
        return true;
    }

    /**
     * Hook for subclasses to implement card-specific rules for potential play
     */
    protected abstract boolean canBearThis(PlannedBoardState plannedBoardState, PhysicalCard target);

    protected abstract boolean playsToSupportArea();

    private boolean uniqueRequirementOk(PlannedBoardState plannedBoardState) {
        return !plannedBoardState.sameTitleInPlayOrInDeadPile(self.getBlueprint().getTitle(), self.getOwner());
    }

    public boolean isValidBearer(BotCard target, PlannedBoardState plannedBoardState) {
        return isValidBearer(target.getSelf(), plannedBoardState);
    }

    public boolean isValidBearer(PhysicalCard target) {
        return isValidBearer(target, new PlannedBoardState(target.getGame()));
    }

    public boolean isValidBearer(PhysicalCard target, PlannedBoardState plannedBoardState) {
        return canBearThis(plannedBoardState, target) && isCharacter(target) && hasFreeSlotForThis(plannedBoardState, target);
    }

    private boolean isCharacter(PhysicalCard target) {
        CardType ct = target.getBlueprint().getCardType();
        return ct.equals(CardType.COMPANION) || ct.equals(CardType.ALLY) || ct.equals(CardType.MINION);
    }

    private boolean hasFreeSlotForThis(PlannedBoardState plannedBoardState, PhysicalCard target) {
        Set<PossessionClass> classes = self.getBlueprint().getPossessionClasses();
        if (classes == null) {
            return true;
        }
        return plannedBoardState.hasFreeSlotForThis(target, classes);
    }
}
