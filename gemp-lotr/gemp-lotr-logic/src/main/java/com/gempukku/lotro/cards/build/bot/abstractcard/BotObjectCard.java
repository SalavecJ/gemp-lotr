package com.gempukku.lotro.cards.build.bot.abstractcard;

import com.gempukku.lotro.cards.build.bot.RequirementsUtility;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.PossessionClass;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

import java.util.Set;

public abstract class BotObjectCard extends BotCard {
    public BotObjectCard(PhysicalCard self) {
        super(self);
    }

    @Override
    public boolean canBePlayed(LotroGame game) {
        if (!phaseOk(game)) return false;
        if (!uniqueRequirementOk(game)) return false;
        if (!playsToSupportArea()
                && !RequirementsUtility.canSpot(game, self.getOwner(), this::isValidBearer))
            return false;
        return otherRequirementsNowOk(game);
    }

    @Override
    public boolean canEverBePlayed(LotroGame game) {
        if (!RequirementsUtility.canSpotEver(game, self.getOwner(), this::isValidBearer))
            return false;
        return otherRequirementsEverOk(game);
    }

    /**
     * Hook for subclasses to implement card-specific rules for current board state
     */
    protected boolean otherRequirementsNowOk(LotroGame game) {
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
    protected abstract boolean canBearThis(LotroGame game, PhysicalCard target);

    protected abstract boolean playsToSupportArea();

    private boolean phaseOk(LotroGame game) {
        if (Side.FREE_PEOPLE.equals(self.getBlueprint().getSide())) {
            return Phase.FELLOWSHIP.equals(game.getGameState().getCurrentPhase());
        } else if (Side.SHADOW.equals(self.getBlueprint().getSide())) {
            return Phase.SHADOW.equals(game.getGameState().getCurrentPhase());
        }
        throw new IllegalStateException("Character cards need to have side: " + self.getBlueprint().getFullName());
    }

    private boolean uniqueRequirementOk(LotroGame game) {
        if (!self.getBlueprint().isUnique()) return true;

        boolean sameNameInPlay = game.getGameState().getInPlay().stream()
                .anyMatch(pc -> pc.getOwner().equals(self.getOwner())
                        && pc.getBlueprint().getTitle().equals(self.getBlueprint().getTitle()));

        boolean sameNameDead = game.getGameState().getDeadPile(self.getOwner()).stream()
                .anyMatch(pc -> pc.getBlueprint().getTitle().equals(self.getBlueprint().getTitle()));

        return !sameNameInPlay && !sameNameDead;
    }

    public boolean isValidBearer(PhysicalCard target) {
        LotroGame game = target.getGame();
        return canBearThis(game, target) && isCharacter(target) && hasFreeSlotForThis(game, target);
    }

    private boolean isCharacter(PhysicalCard target) {
        CardType ct = target.getBlueprint().getCardType();
        return ct.equals(CardType.COMPANION) || ct.equals(CardType.ALLY) || ct.equals(CardType.MINION);
    }

    private boolean hasFreeSlotForThis(LotroGame game, PhysicalCard target) {
        Set<PossessionClass> classes = self.getBlueprint().getPossessionClasses();
        if (classes == null) {
            return true;
        }
        for (PossessionClass possessionClass : classes) {
            for (PhysicalCard attachedCard : game.getGameState().getAttachedCards(target)) {
                if (attachedCard.getBlueprint().getPossessionClasses() != null
                        && attachedCard.getBlueprint().getPossessionClasses().contains(possessionClass)) {
                    return false;
                }
            }
        }
        return true;
    }
}
