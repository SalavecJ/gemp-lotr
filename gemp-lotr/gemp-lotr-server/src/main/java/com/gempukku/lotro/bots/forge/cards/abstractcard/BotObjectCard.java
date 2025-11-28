package com.gempukku.lotro.bots.forge.cards.abstractcard;

import com.gempukku.lotro.bots.forge.cards.BotCardFactory;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.PossessionClass;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.Set;

public abstract class BotObjectCard extends BotCard {
    public BotObjectCard(PhysicalCard self) {
        super(self);
    }

    @Override
    public boolean canBePlayedNoMatterThePhase(DefaultLotroGame game) {
        if (!uniqueRequirementOk(game)) return false;
        if (!playsToSupportArea()
                && !game.getGameState().canSpot(self.getOwner(), botCard -> isValidBearer(botCard, game))
                && !isValidPlayableBearerInHand(game))
            return false;
        return true;
    }

    @Override
    public boolean canBePlayed(DefaultLotroGame game) {
        return super.canBePlayed(game)
                && (playsToSupportArea() || game.getGameState().canSpot(self.getOwner(), botCard -> isValidBearer(botCard, game)));
    }

    /**
     * Hook for subclasses to implement card-specific rules for potential play
     */
    protected abstract boolean canBearThis(DefaultLotroGame game, PhysicalCard target);

    protected abstract boolean playsToSupportArea();

    private boolean isValidPlayableBearerInHand(DefaultLotroGame game) {
        return game.getGameState().getHand(self.getOwner()).stream().filter(card -> isValidBearer(card, game)).anyMatch(card -> BotCardFactory.create(card).canBePlayedNoMatterThePhase(game));
    }

    public boolean isValidBearer(PhysicalCard target, DefaultLotroGame game) {
        return canBearThis(game, target) && isCharacter(target) && hasFreeSlotForThis(game, target);
    }

    private boolean isCharacter(PhysicalCard target) {
        CardType ct = target.getBlueprint().getCardType();
        return ct.equals(CardType.COMPANION) || ct.equals(CardType.ALLY) || ct.equals(CardType.MINION);
    }

    private boolean hasFreeSlotForThis(DefaultLotroGame game, PhysicalCard target) {
        Set<PossessionClass> classes = self.getBlueprint().getPossessionClasses();
        if (classes == null) {
            return true;
        }
        return game.getGameState().hasFreeSlotForThis(target, classes);
    }
}
