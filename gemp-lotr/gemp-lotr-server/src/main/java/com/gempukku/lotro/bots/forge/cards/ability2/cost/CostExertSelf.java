package com.gempukku.lotro.bots.forge.cards.ability2.cost;

import com.gempukku.lotro.bots.forge.cards.ability2.util.WoundsValueUtil;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

public class CostExertSelf extends Cost {
    private final PhysicalCard self;
    protected final int amount;

    public CostExertSelf(PhysicalCard self, int amount) {
        this.self = self;
        this.amount = amount;
    }

    @Override
    public boolean canPayCost(String player, DefaultLotroGame game) {
        return game.getModifiersQuerying().getVitality(game, self) > amount;
    }

    @Override
    public double getValueIfPayed(String player, DefaultLotroGame game) {
        if (!canPayCost(player, game)) {
            throw new IllegalStateException("Cost cannot be payed");
        }

        int amount = Math.min(this.amount, game.getModifiersQuerying().getVitality(game, self) - 1);
        return WoundsValueUtil.evaluateWoundsChangeValue(player, game, self, amount);
    }

    @Override
    public String toString(String player, DefaultLotroGame game) {
        if (amount == 1) {
            return "exert self (" + self.getBlueprint().getFullName() + ")";
        } else {
            return "exert self " + amount + " times (" + self.getBlueprint().getFullName() + ")";
        }
    }
}
