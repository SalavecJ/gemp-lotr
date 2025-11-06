package com.gempukku.lotro.bots.forge.plan.action;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

import java.util.*;

/**
 * Action representing assigning a specific minion to a specific FP character.
 * During assignment phase, the FP player assigns minions to companions/allies first,
 * then the Shadow player can assign remaining minions to existing skirmishes.
 *
 * This action is used during planning/minimax exploration to build up an assignment map.
 * It does NOT directly execute against an AwaitingDecision - instead, the complete
 * assignment map is built up in PlannedBoardState and submitted when both players pass.
 */
public class AssignMinionAction implements ActionToTake {
    private final PhysicalCard minion;
    private final PhysicalCard fpCharacter;

    public AssignMinionAction(PhysicalCard minion, PhysicalCard fpCharacter) {
        this.minion = minion;
        this.fpCharacter = fpCharacter;
    }

    public PhysicalCard getMinion() {
        return minion;
    }

    public PhysicalCard getFpCharacter() {
        return fpCharacter;
    }

    /**
     * Converts a list of AssignMinionActions to the Map format required by PlayerAssignMinionsDecision.
     * Multiple minions can be assigned to the same FP character, and they will be grouped together.
     *
     * @param actions List of assignment actions to convert
     * @return Map where key is FP character card ID (as String) and value is list of minion card IDs (as Strings)
     */
    public static Map<String, List<String>> toAssignmentMap(List<AssignMinionAction> actions) {
        Map<String, List<String>> assignmentMap = new HashMap<>();

        for (AssignMinionAction action : actions) {
            String fpCharacterId = String.valueOf(action.getFpCharacter().getCardId());
            String minionId = String.valueOf(action.getMinion().getCardId());

            assignmentMap.computeIfAbsent(fpCharacterId, k -> new ArrayList<>()).add(minionId);
        }

        return assignmentMap;
    }

    @Override
    public int carryOut(AwaitingDecision awaitingDecision) {
        // AssignMinionAction doesn't directly execute against a decision.
        // The actual execution happens when the complete assignment map is submitted
        throw new UnsupportedOperationException(
            "AssignMinionAction cannot be carried out directly."
        );
    }

    @Override
    public String toString() {
        return "Action: Assign " + minion.getBlueprint().getFullName() +
               " to " + fpCharacter.getBlueprint().getFullName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssignMinionAction that = (AssignMinionAction) o;
        return minion.getCardId() == that.minion.getCardId() &&
               fpCharacter.getCardId() == that.fpCharacter.getCardId();
    }

    @Override
    public int hashCode() {
        return 31 * minion.getCardId() + fpCharacter.getCardId();
    }
}

