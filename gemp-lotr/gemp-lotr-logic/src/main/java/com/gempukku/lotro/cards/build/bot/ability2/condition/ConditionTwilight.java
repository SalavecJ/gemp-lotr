package com.gempukku.lotro.cards.build.bot.ability2.condition;

import com.gempukku.lotro.cards.build.bot.abstractcard.BotCard;
import com.gempukku.lotro.game.state.PlannedBoardState;

public class ConditionTwilight extends Condition {
    public enum TwilightState {
        GREATER_THAN, LESS_THAN
    }

    protected final TwilightState twilightState;
    protected final int amount;

    public ConditionTwilight(TwilightState twilightState, int amount) {
        this.twilightState = twilightState;
        this.amount = amount;
    }

    @Override
    public boolean isOk(BotCard source, PlannedBoardState plannedBoardState) {
        if (twilightState.equals(TwilightState.LESS_THAN)) {
            return plannedBoardState.getTwilight() < amount;
        } else if (twilightState.equals(TwilightState.GREATER_THAN)) {
            return plannedBoardState.getTwilight() > amount;
        }

        throw new IllegalStateException("Unknown twilight state");
    }
}
