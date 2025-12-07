package com.gempukku.lotro.bots.forge.cards.ability2.util;

import com.gempukku.lotro.logic.timing.DefaultLotroGame;

public class BurdensValueUtil {
    /**
     * Evaluates the value of adding or removing burdens.
     *
     * @param player The player evaluating the burden change
     * @param game The current game state
     * @param amount The number of burdens changed (positive = add, negative = remove)
     * @return The value of this burden change from the player's perspective (positive = good for player)
     */
    public static double evaluateBurdenChangeValue(String player, DefaultLotroGame game, int amount) {
        if (amount == 0) {
            return 0.0;
        }

        int currentBurdens = game.getGameState().getBurdens();

        // Calculate the actual change (can't go below 0)
        int realChange;
        if (amount > 0) {
            // adding burdens
            realChange = amount;
        } else {
            // removing burdens (can't remove more than exist)
            realChange = Math.max(amount, -currentBurdens);
        }

        if (realChange == 0) {
            return 0.0;
        }

        // Base value per burden
        double value = Math.abs(realChange);

        // Evaluate based on burden count after the change
        int burdensAfterChange = currentBurdens + realChange;

        // Burdens are more impactful at higher counts
        if (realChange > 0) {
            value *= 0.8 + (0.2 * burdensAfterChange);
        } else {
            value *= 0.8 + (0.2 * currentBurdens);
        }

        // Determine if this is good or bad for the player
        boolean isShadowPlayer = player.equals(game.getGameState().getCurrentShadowPlayer());
        boolean addingBurdens = realChange > 0;

        // Shadow wants to add burdens, FP wants to remove them
        if (isShadowPlayer == addingBurdens) {
            return value; // Good for this player
        } else {
            return -value; // Bad for this player
        }
    }
}

