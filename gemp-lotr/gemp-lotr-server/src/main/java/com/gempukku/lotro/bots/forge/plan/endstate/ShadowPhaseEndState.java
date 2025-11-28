package com.gempukku.lotro.bots.forge.plan.endstate;

import com.gempukku.lotro.bots.forge.plan.CombatOutcome;
import com.gempukku.lotro.bots.forge.plan.action2.ActionToTake2;
import com.gempukku.lotro.bots.forge.utils.ActionFinderUtil;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.Collections;
import java.util.List;

public class ShadowPhaseEndState extends PhaseEndState {
    private AfterCombatEndPhase afterCombatEndPhase;

    public ShadowPhaseEndState(DefaultLotroGame game, List<ActionToTake2> shadowActions) {
        // Shadow phase only has shadow actions, FP actions is empty
        super(game, Collections.emptyList(), shadowActions);
    }

    /**
     * Returns true if the combat path can win the game.
     */
    public boolean hasPotentialToWinTheGame() {
        return new CombatOutcome(copy, getAfterCombatEndPhase().copy).winsTheGame();
    }

    /**
     * Gets the best combat path from this shadow phase end state.
     * Contains all 5 combat phase end states and can produce a combat outcome.
     * Uses lazy evaluation - only computes the path when first accessed.
     */
    public AfterCombatEndPhase getAfterCombatEndPhase() {
        if (afterCombatEndPhase == null) {
            // Explore the combat tree to find the best combat path
            afterCombatEndPhase = ActionFinderUtil.findBestCombatPath(copy);
        }
        return afterCombatEndPhase;
    }

    @Override
    public double getValue() {
        if (value != 0.0)
            return value;
        return new CombatOutcome(copy, afterCombatEndPhase.copy).evaluateOutcome();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Shadow Phase Actions:\n");

        if (getShadowActions().isEmpty()) {
            sb.append("  (no actions)");
        } else {
            for (int i = 0; i < getShadowActions().size(); i++) {
                ActionToTake2 action = getShadowActions().get(i);
                sb.append("  ").append(i + 1).append(". ").append(action.toString()).append("\n");
            }
        }

        sb.append("\nCombat Outcome:\n");
        sb.append(new CombatOutcome(copy, afterCombatEndPhase.copy).toString());

        return sb.toString();
    }
}

