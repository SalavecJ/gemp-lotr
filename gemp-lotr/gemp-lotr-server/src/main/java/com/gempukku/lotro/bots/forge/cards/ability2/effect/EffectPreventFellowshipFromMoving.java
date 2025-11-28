package com.gempukku.lotro.bots.forge.cards.ability2.effect;

import com.gempukku.lotro.logic.modifiers.ModifierFlag;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;
import com.gempukku.lotro.logic.timing.RuleUtils;

public class EffectPreventFellowshipFromMoving extends Effect {

    public EffectPreventFellowshipFromMoving() {

    }


    @Override
    public double getValueIfResolved(String player, DefaultLotroGame game) {
        if (game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.CANT_MOVE)) {
            return 0;
        }
        if (game.getGameState().getMoveCount() >= RuleUtils.calculateMoveLimit(game)) {
            return 0;
        }
        return 5;
    }

    @Override
    public String toString(String player, DefaultLotroGame game) {
        return "prevent fellowship from moving";
    }
}
