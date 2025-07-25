package com.gempukku.lotro.bots.rl.v2.learning.assignment;

import com.gempukku.lotro.game.state.GameState;

import java.util.*;

public class ShadowAssignmentTrainer extends AbstractAssignmentTrainer {
    @Override
    protected boolean isForFp() {
        return false;
    }

    @Override
    protected void generateAssignmentsRecursive(List<String> minionsToAssign,
                                                int index,
                                                List<String> freeChars,
                                                AssignmentInfo current,
                                                List<AssignmentInfo> results,
                                                GameState gameState) {
        if (index >= minionsToAssign.size()) {
            // Deep copy the current assignment
            AssignmentInfo copy = new AssignmentInfo(new HashMap<>(), new HashMap<>(), 0, 0);
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
            current.madeAssignment().get(fpPhysId).add(minionPhysId);
            current.wholeAssignment().putIfAbsent(fpPhysId, new ArrayList<>());
            current.wholeAssignment().get(fpPhysId).add(minionPhysId);
            generateAssignmentsRecursive(minionsToAssign, index + 1, freeChars, current, results, gameState);
            current.madeAssignment().get(fpPhysId).removeLast(); // backtrack
            current.wholeAssignment().get(fpPhysId).removeLast(); // backtrack
        }
    }
}
