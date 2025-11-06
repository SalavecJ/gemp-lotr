package com.gempukku.lotro.bots.forge.plan.endstate;

import com.gempukku.lotro.bots.forge.plan.CombatPath;
import com.gempukku.lotro.bots.forge.plan.action.ActionToTake;
import com.gempukku.lotro.bots.forge.utils.ActionFinderUtil;
import com.gempukku.lotro.game.state.PlannedBoardState;

import java.util.Collections;
import java.util.List;

public class ShadowPhaseEndState extends PhaseEndState {
    private final CombatPath combatPath;

    public ShadowPhaseEndState(PlannedBoardState finalBoardState, List<ActionToTake> shadowActions) {
        // Shadow phase only has shadow actions, FP actions is empty
        super(finalBoardState, Collections.emptyList(), shadowActions);

        // Explore the combat tree to find the best combat path
        this.combatPath = ActionFinderUtil.findBestCombatPath(finalBoardState);
    }

    /**
     * Returns true if the combat path can win the game.
     */
    public boolean hasPotentialToWinTheGame() {
        if (combatPath == null) {
            return false; // Temporary until combat exploration is implemented
        }
        return combatPath.getCombatOutcome().winsTheGame();
    }

    /**
     * Gets the best combat path from this shadow phase end state.
     * Contains all 5 combat phase end states and can produce a combat outcome.
     */
    public CombatPath getCombatPath() {
        return combatPath;
    }
}

