package com.gempukku.lotro.cards.build.bot.abstractcard;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

public abstract class BotCharacterCard extends BotCard {
    public BotCharacterCard(PhysicalCard self) {
        super(self);
    }

    @Override
    public boolean canBePlayed(LotroGame game) {
        if (!phaseOk(game)) return false;
        if (!uniqueRequirementOk(game)) return false;
        return otherRequirementsNowOk(game);
    }

    @Override
    public boolean canEverBePlayed(LotroGame game) {
        if (Side.FREE_PEOPLE.equals(self.getBlueprint().getSide()) && !uniqueRequirementOk(game)) return false;
        return otherRequirementsEverOk(game);
    }

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

    /** Hook for subclasses to implement card-specific rules for current board state */
    protected boolean otherRequirementsNowOk(LotroGame game) {
        return true;
    }

    /** Hook for subclasses to implement card-specific rules for potential play */
    protected boolean otherRequirementsEverOk(LotroGame game) {
        return true;
    }
}
