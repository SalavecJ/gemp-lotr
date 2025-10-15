package com.gempukku.lotro.cards.build.bot.ability2.effect;

import com.gempukku.lotro.cards.build.bot.abstractcard.BotCard;
import com.gempukku.lotro.game.state.PlannedBoardState;

public class EffectRevealOpponentsHand extends Effect{

    public EffectRevealOpponentsHand() {

    }

    @Override
    public void resolve(BotCard source, PlannedBoardState plannedBoardState) {
        // TODO when bot can use the information
    }

    @Override
    public double getValueIfResolved(BotCard source, PlannedBoardState plannedBoardState) {
        // TODO when bot can use the information
        return 0.3;
    }
}
