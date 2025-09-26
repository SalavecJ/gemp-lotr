package com.gempukku.lotro.cards.build.bot.abstractcard;

import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.state.PlannedBoardState;

public abstract class BotCharacterCard extends BotCard {
    public BotCharacterCard(PhysicalCard self) {
        super(self);
    }

    @Override
    public boolean canBePlayed(PlannedBoardState plannedBoardState) {
        if (!uniqueRequirementOk(plannedBoardState)) return false;
        return otherRequirementsNowOk(plannedBoardState);
    }

    @Override
    public boolean canEverBePlayed() {
        if (Side.FREE_PEOPLE.equals(self.getBlueprint().getSide()) && !uniqueRequirementOk(self.getGame())) return false;
        return otherRequirementsEverOk(self.getGame());
    }
    private boolean uniqueRequirementOk(PlannedBoardState plannedBoardState) {
        return !plannedBoardState.sameTitleInPlayOrInDeadPile(self.getBlueprint().getTitle(), self.getOwner());
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
    protected boolean otherRequirementsNowOk(PlannedBoardState plannedBoardState) {
        return true;
    }

    /** Hook for subclasses to implement card-specific rules for potential play */
    protected boolean otherRequirementsEverOk(LotroGame game) {
        return true;
    }
}
