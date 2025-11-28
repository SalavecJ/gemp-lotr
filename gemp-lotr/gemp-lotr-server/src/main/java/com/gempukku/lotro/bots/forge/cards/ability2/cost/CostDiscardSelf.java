package com.gempukku.lotro.bots.forge.cards.ability2.cost;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

public class CostDiscardSelf extends Cost {
    private final PhysicalCard self;

    public CostDiscardSelf(PhysicalCard self) {
        this.self = self;
    }


    @Override
    public boolean canPayCost(String player, DefaultLotroGame game) {
        return game.getGameState().getInPlay().contains(self);
    }

    @Override
    public double getValueIfPayed(String player, DefaultLotroGame game) {
        if (!canPayCost(player, game)) {
            throw new IllegalStateException("Cost cannot be payed");
        }

        return -1.1;
    }

    @Override
    public String toString(String player, DefaultLotroGame game) {
        return "discard self (" + self.getBlueprint().getFullName() + ")";
    }
}
