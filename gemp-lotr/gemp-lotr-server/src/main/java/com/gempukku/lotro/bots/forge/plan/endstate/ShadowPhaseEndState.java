package com.gempukku.lotro.bots.forge.plan.endstate;

import com.gempukku.lotro.bots.forge.plan.CombatPath;
import com.gempukku.lotro.bots.forge.plan.action.ActionToTake;
import com.gempukku.lotro.bots.forge.utils.ActionFinderUtil;
import com.gempukku.lotro.game.state.PlannedBoardState;

import java.util.Collections;
import java.util.List;

public class ShadowPhaseEndState extends PhaseEndState {
    private CombatPath cachedCombatPath = null;

    public ShadowPhaseEndState(PlannedBoardState finalBoardState, List<ActionToTake> shadowActions) {
        // Shadow phase only has shadow actions, FP actions is empty
        super(finalBoardState, Collections.emptyList(), shadowActions);
    }

    /**
     * Returns true if the combat path can win the game.
     */
    public boolean hasPotentialToWinTheGame() {
        return getCombatPath().getCombatOutcome().winsTheGame();
    }

    /**
     * Gets the best combat path from this shadow phase end state.
     * Contains all 5 combat phase end states and can produce a combat outcome.
     * Uses lazy evaluation - only computes the path when first accessed.
     */
    public CombatPath getCombatPath() {
        if (cachedCombatPath == null) {
            // Explore the combat tree to find the best combat path
            cachedCombatPath = ActionFinderUtil.findBestCombatPath(getBoardState());
        }
        return cachedCombatPath;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Shadow Phase Actions:\n");

        if (getShadowActions().isEmpty()) {
            sb.append("  (no actions)");
        } else {
            for (int i = 0; i < getShadowActions().size(); i++) {
                ActionToTake action = getShadowActions().get(i);
                sb.append("  ").append(i + 1).append(". ").append(action.toString()).append("\n");
            }
        }

        sb.append("\nCombat Outcome:\n");
        sb.append(getCombatPath().toString());

        return sb.toString();
    }
}

