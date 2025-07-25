package com.gempukku.lotro.bots.rl.v2.learning.assignment;

import com.gempukku.lotro.bots.BotService;
import com.gempukku.lotro.bots.rl.v2.ModelRegistryV2;
import com.gempukku.lotro.bots.rl.v2.learning.AbstractTrainerV2;
import com.gempukku.lotro.bots.rl.v2.learning.SavedVector;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import smile.classification.SoftClassifier;

import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractAssignmentTrainer extends AbstractTrainerV2 {
    private static final String PASS = "pass";

    protected abstract boolean isForFp();

    protected abstract void generateAssignmentsRecursive(List<String> minionsToAssign,
                                                         int index,
                                                         List<String> freeChars,
                                                         AssignmentInfo current,
                                                         List<AssignmentInfo> results,
                                                         GameState gameState);

    @Override
    public boolean appliesTo(GameState gameState, AwaitingDecision decision, String playerName) {
        if (!decision.getDecisionType().equals(AwaitingDecisionType.ASSIGN_MINIONS)) {
            return false;
        }
        return  (isForFp() && gameState.getCurrentPlayerId().equals(playerName)) ||
                (!isForFp() && !gameState.getCurrentPlayerId().equals(playerName));
    }

    @Override
    public String getAnswer(GameState gameState, AwaitingDecision decision, String playerName, ModelRegistryV2 modelRegistry) {
        Map<String, String[]> params = decision.getDecisionParameters();
        String[] freeCharIds = params.get("freeCharacters");
        String[] minionIds = params.get("minions");

        if (freeCharIds == null || minionIds == null || freeCharIds.length == 0 || minionIds.length == 0)
            return "";

        if (modelRegistry == null) {
            throw new UnsupportedOperationException("Model not found for " + getName());
        }
        SoftClassifier<double[]> model = modelRegistry.getModel(getName());
        if (model == null) {
            throw new UnsupportedOperationException("Model not found for " + getName());
        }


        HashMap<String, List<String>> alreadyAssignedMap = new HashMap<>();
        gameState.getAssignments().forEach(assignment -> {
            String id = String.valueOf(assignment.getFellowshipCharacter().getCardId());
            alreadyAssignedMap.put(id, new ArrayList<>());
            assignment.getShadowCharacters().forEach(shadow -> alreadyAssignedMap.get(id).add(String.valueOf(shadow.getCardId())));
        });
        gameState.getInPlay().forEach(fpCharacter -> {
            if ((fpCharacter.getBlueprint().getCardType().equals(CardType.COMPANION) ||
                    fpCharacter.getBlueprint().getCardType().equals(CardType.ALLY)) &&
                    fpCharacter.getOwner().equals(gameState.getCurrentPlayerId())) {
                if (!alreadyAssignedMap.containsKey(String.valueOf(fpCharacter.getCardId()))) {
                    alreadyAssignedMap.put(String.valueOf(fpCharacter.getCardId()), new ArrayList<>());
                }
            }
        });

        List<AssignmentInfo> allAssignments = generateAllAssignments(alreadyAssignedMap, Arrays.stream(freeCharIds).toList(), Arrays.stream(minionIds).toList(), gameState);
        double[] stateVector = extractFeatures(gameState, decision, playerName);

        double bestScore = Double.NEGATIVE_INFINITY;
        Map<String, List<String>> bestAssignment = null;

        for (AssignmentInfo assignment : allAssignments) {
            try {
                double[] assignmentVector = getAssignmentFeatures(gameState, assignment.wholeAssignment(),
                        assignment.numberOfUnassignedMinions(), assignment.strengthOfUnassignedMinions(), playerName);
                double[] extended = Arrays.copyOf(stateVector, stateVector.length + assignmentVector.length);
                System.arraycopy(assignmentVector, 0, extended, stateVector.length, assignmentVector.length);

                double[] probs = new double[2];
                model.predict(extended, probs);
                if (probs[1] > bestScore) {
                    bestScore = probs[1];
                    bestAssignment = assignment.madeAssignment();
                }
            } catch (CardNotFoundException ignore) {
            }
        }

        if (bestAssignment == null || bestAssignment.isEmpty()) {
            return "";
        }

        return bestAssignment.entrySet().stream()
                .map(entry -> entry.getKey() + " " + String.join(" ", entry.getValue()))
                .collect(Collectors.joining(","));
    }

    private double[] getAssignmentFeatures(GameState gameState, Map<String, List<String>> assignmentMap,
                                           int numberOfUnassignedMinions, int strengthOfUnassignedMinions,
                                           String playerName) throws CardNotFoundException {
        List<Double> tbr = new ArrayList<>();

        List<String> companions = assignmentMap.keySet().stream().filter(s -> {
            try {
                return BotService.staticLibrary.getLotroCardBlueprint(gameState.getBlueprintId(Integer.parseInt(s))).getCardType().equals(CardType.COMPANION);
            } catch (CardNotFoundException e) {
                return false;
            }
        }).collect(Collectors.toList());
        List<String> allies = assignmentMap.keySet().stream().filter(s -> {
            try {
                return BotService.staticLibrary.getLotroCardBlueprint(gameState.getBlueprintId(Integer.parseInt(s))).getCardType().equals(CardType.ALLY);
            } catch (CardNotFoundException e) {
                return false;
            }
        }).collect(Collectors.toList());

        companions.sort((o1, o2) -> {
            try {
                return Integer.compare(BotService.staticLibrary.getLotroCardBlueprint(gameState.getBlueprintId(Integer.parseInt(o1))).getStrength(), BotService.staticLibrary.getLotroCardBlueprint(gameState.getBlueprintId(Integer.parseInt(o2))).getStrength());
            } catch (CardNotFoundException e) {
                return 0;
            }
        });
        allies.sort((o1, o2) -> {
            try {
                return Integer.compare(BotService.staticLibrary.getLotroCardBlueprint(gameState.getBlueprintId(Integer.parseInt(o1))).getStrength(), BotService.staticLibrary.getLotroCardBlueprint(gameState.getBlueprintId(Integer.parseInt(o2))).getStrength());
            } catch (CardNotFoundException e) {
                return 0;
            }
        });

        while (companions.size() < 9) {
            companions.add(PASS);
        }
        while (allies.size() < 4) {
            allies.add(PASS);
        }

        final int[] companionsAdded = {0};
        companions.forEach(companion -> {
            if (companionsAdded[0] >= 9) {
                return;
            }
            companionsAdded[0]++;
            if (companion.equals(PASS)) {
                tbr.addAll(Arrays.stream(new double[9]).boxed().toList());
            } else {
                try {
                    double[] thisAssignment =
                            BotService.staticLibrary.getLotroCardBlueprint(gameState.getBlueprintId(Integer.parseInt(companion)))
                                    .getFpAssignedCardFeatures(gameState, Integer.parseInt(companion), playerName, assignmentMap.get(companion).stream().mapToInt(Integer::parseInt).boxed().toList());
                    tbr.addAll(Arrays.stream(thisAssignment).boxed().toList());
                } catch (CardNotFoundException e) {
                    tbr.addAll(Arrays.stream(new double[9]).boxed().toList());
                }
            }
        });

        final int[] alliesAdded = {0};
        allies.forEach(ally -> {
            if (alliesAdded[0] >= 4) {
                return;
            }
            alliesAdded[0]++;
            if (ally.equals(PASS)) {
                tbr.addAll(Arrays.stream(new double[9]).boxed().toList());
            } else {

                try {
                    double[] thisAssignment =
                            BotService.staticLibrary.getLotroCardBlueprint(gameState.getBlueprintId(Integer.parseInt(ally)))
                                    .getFpAssignedCardFeatures(gameState, Integer.parseInt(ally), playerName, assignmentMap.get(ally).stream().mapToInt(Integer::parseInt).boxed().toList());
                    tbr.addAll(Arrays.stream(thisAssignment).boxed().toList());
                } catch (CardNotFoundException e) {
                    tbr.addAll(Arrays.stream(new double[9]).boxed().toList());
                }
            }
        });

        tbr.add((double) numberOfUnassignedMinions);
        tbr.add((double) strengthOfUnassignedMinions);

        return tbr.stream().mapToDouble(Double::doubleValue).toArray();

    }

    private List<AssignmentInfo> generateAllAssignments(HashMap<String, List<String>> alreadyAssignedMap,
                                                        List<String> freeChars,
                                                        List<String> minions,
                                                        GameState gameState) {
        List<AssignmentInfo> results = new ArrayList<>();

        // Deep copy to avoid mutating input map
        AssignmentInfo current = new AssignmentInfo(new HashMap<>(), new HashMap<>(), 0 , 0);
        for (String fp : alreadyAssignedMap.keySet()) {
            current.wholeAssignment().put(fp, new ArrayList<>(alreadyAssignedMap.get(fp)));
        }

        generateAssignmentsRecursive(minions, 0, freeChars, current, results, gameState);
        return results;
    }

    @Override
    public List<SavedVector> toStringVectors(GameState gameState, AwaitingDecision decision, String playerId, String answer) {
        Map<String, String[]> params = decision.getDecisionParameters();
        String[] minionIds = params.get("minions");


        HashMap<String, List<String>> alreadyAssignedMap = new HashMap<>();
        gameState.getAssignments().forEach(assignment -> {
            String id = String.valueOf(assignment.getFellowshipCharacter().getCardId());
            alreadyAssignedMap.put(id, new ArrayList<>());
            assignment.getShadowCharacters().forEach(shadow -> alreadyAssignedMap.get(id).add(String.valueOf(shadow.getCardId())));
        });
        gameState.getInPlay().forEach(fpCharacter -> {
            if ((fpCharacter.getBlueprint().getCardType().equals(CardType.COMPANION) ||
                    fpCharacter.getBlueprint().getCardType().equals(CardType.ALLY)) &&
                    fpCharacter.getOwner().equals(gameState.getCurrentPlayerId())) {
                if (!alreadyAssignedMap.containsKey(String.valueOf(fpCharacter.getCardId()))) {
                    alreadyAssignedMap.put(String.valueOf(fpCharacter.getCardId()), new ArrayList<>());
                }
            }
        });

        HashMap<String, List<String>> assignmentMap = new HashMap<>();
        String[] assignments = answer.split(",");
        for (String assignment : assignments) {
            String[] cards = assignment.split(" ");
            String fp = cards[0];
            List<String> minions = new ArrayList<>(Arrays.asList(cards).subList(1, cards.length));
            assignmentMap.put(fp, minions);
        }

        alreadyAssignedMap.forEach((fp, shadow) -> {
            if (!assignmentMap.containsKey(fp)) {
                assignmentMap.put(fp, new ArrayList<>());
            }
            assignmentMap.get(fp).addAll(shadow);
        });


        int numberOfUnassignedMinions;
        int strengthOfUnassignedMinions;

        List<String> unassigned = new ArrayList<>();
        for (String minionId : minionIds) {
            if (!answer.contains(minionId)) {
                unassigned.add(minionId);
            }
        }
        numberOfUnassignedMinions = unassigned.size();
        strengthOfUnassignedMinions = unassigned.stream().mapToInt(minionId -> {
            for (PhysicalCard physicalCard : gameState.getInPlay()) {
                if (physicalCard.getCardId() == Integer.parseInt(minionId)) {
                    return physicalCard.getBlueprint().getStrength();
                }
            }
            return 0;
        }).sum();

        double[] stateVector = extractFeatures(gameState, decision, playerId);

        try {
            double[] assignmentVector = getAssignmentFeatures(gameState, assignmentMap,
                    numberOfUnassignedMinions, strengthOfUnassignedMinions, playerId);
            return List.of(new SavedVector(getName(), stateVector, assignmentVector, List.of()));
        } catch (CardNotFoundException ignored) {
            return List.of();
        }
    }

    protected record AssignmentInfo(Map<String, List<String>> wholeAssignment,
                                    Map<String, List<String>> madeAssignment,
                                    int numberOfUnassignedMinions,
                                    int strengthOfUnassignedMinions) {
    }
}
