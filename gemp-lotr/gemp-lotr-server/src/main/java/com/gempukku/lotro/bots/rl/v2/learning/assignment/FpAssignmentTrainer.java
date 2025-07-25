package com.gempukku.lotro.bots.rl.v2.learning.assignment;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.GameState;

import java.util.*;

public class FpAssignmentTrainer extends AbstractAssignmentTrainer {
    @Override
    protected boolean isForFp() {
        return true;
    }

    @Override
    protected void generateAssignmentsRecursive(List<String> minionsToAssign,
                                                int index,
                                                List<String> freeChars,
                                                AssignmentInfo current,
                                                List<AssignmentInfo> results,
                                                GameState gameState) {
        generateAssignmentsRecursive(minionsToAssign, index, freeChars, current, results, new ArrayList<>(), gameState);
    }

    private void generateAssignmentsRecursive(List<String> minionsToAssign,
                                              int index,
                                              List<String> freeChars,
                                              AssignmentInfo current,
                                              List<AssignmentInfo> results,
                                              List<String> unassignedMinions,
                                              GameState gameState) {
        if (index >= minionsToAssign.size()) {
            // Deep copy the current assignment
            int unassignedStrength = unassignedMinions.stream().mapToInt(minionId -> {
                for (PhysicalCard physicalCard : gameState.getInPlay()) {
                    if (physicalCard.getCardId() == Integer.parseInt(minionId)) {
                        return physicalCard.getBlueprint().getStrength();
                    }
                }
                return 0;
            }).sum();
            AssignmentInfo copy = new AssignmentInfo(new HashMap<>(), new HashMap<>(), unassignedMinions.size(), unassignedStrength); // strength placeholder

            for (Map.Entry<String, List<String>> entry : current.wholeAssignment().entrySet()) {
                copy.wholeAssignment().put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
            for (Map.Entry<String, List<String>> entry : current.madeAssignment().entrySet()) {
                copy.madeAssignment().put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }

            results.add(copy);
            return;
        }

        String minionPhysId = minionsToAssign.get(index);

        for (String fpPhysId : freeChars) {

            current.madeAssignment().putIfAbsent(fpPhysId, new ArrayList<>());
            if (current.madeAssignment().get(fpPhysId).isEmpty()) {
                current.madeAssignment().get(fpPhysId).add(minionPhysId);
                current.wholeAssignment().putIfAbsent(fpPhysId, new ArrayList<>());
                current.wholeAssignment().get(fpPhysId).add(minionPhysId);

                generateAssignmentsRecursive(minionsToAssign, index + 1, freeChars, current, results, unassignedMinions, gameState);

                // backtrack
                current.madeAssignment().get(fpPhysId).removeLast();
                current.wholeAssignment().get(fpPhysId).removeLast();
            }
        }

        // Let the current minion remain unassigned
        unassignedMinions.add(minionPhysId);
        generateAssignmentsRecursive(minionsToAssign, index + 1, freeChars, current, results, unassignedMinions, gameState);
        unassignedMinions.removeLast(); // backtrack
    }
}
