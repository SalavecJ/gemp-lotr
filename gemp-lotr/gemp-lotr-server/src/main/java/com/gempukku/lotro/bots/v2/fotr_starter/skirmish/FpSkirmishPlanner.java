package com.gempukku.lotro.bots.v2.fotr_starter.skirmish;

import com.gempukku.lotro.bots.v2.BotLogging;
import com.gempukku.lotro.bots.v2.decision.option.Option;
import com.gempukku.lotro.bots.v2.decision.request.AssignMinionsDecisionRequest;
import com.gempukku.lotro.bots.v2.decision.request.DecisionRequest;
import com.gempukku.lotro.bots.v2.fotr_starter.Plan;
import com.gempukku.lotro.bots.v2.simulation.SimulatedGame;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.*;

import static com.gempukku.lotro.bots.v2.BotLogging.LogLevel.DEBUG;
import static com.gempukku.lotro.bots.v2.BotLogging.LogLevel.REASONING;
import static com.gempukku.lotro.bots.v2.fotr_starter.utility.GameQueries.*;
import static com.gempukku.lotro.bots.v2.fotr_starter.utility.OptionFinders.*;

public class FpSkirmishPlanner {
    public static Plan getPlan(DefaultLotroGame game, String player) {
        SimulatedGame simulation = new SimulatedGame(game);
        DecisionRequest decisionRequest = simulation.getDecisionRequest(player);

        if (decisionRequest instanceof AssignMinionsDecisionRequest assignMinionsDecisionRequest) {
            BotLogging.log(DEBUG, "Planning assignment...");
            List<PhysicalCard> companions = getCompanionsInPlay(simulation, player);
            List<PhysicalCard> minions = getMinionsInPlay(simulation, simulation.getOpponent(player));
            BotLogging.log(REASONING, "Companions:");
            for (PhysicalCard companion : companions) {
                BotLogging.log(REASONING, "  " + companion.getBlueprint().getFullName() + "; S: " + getStrength(simulation, companion) + "; V: " + getVitality(simulation, companion));
            }
            BotLogging.log(REASONING, "Minions:");
            for (PhysicalCard minion : minions) {
                BotLogging.log(REASONING, "  " + minion.getBlueprint().getFullName() + "; S: " + getStrength(simulation, minion) + "; V: " + getVitality(simulation, minion));
            }

            // If swarmed
            List<PhysicalCard> minionsToAssign = new ArrayList<>(minions);
            List<PhysicalCard> minionsToLeaveUnassigned = new ArrayList<>();
            while (minionsToAssign.size() > companions.size()) {
                // Find the minions with the lowest strength to leave unassigned
                PhysicalCard minionToLeaveUnassigned = minionsToAssign.stream().min(Comparator.comparingInt(m -> getStrength(simulation, m))).orElse(null);
                minionsToAssign.remove(minionToLeaveUnassigned);
                minionsToLeaveUnassigned.add(minionToLeaveUnassigned);
            }

            Map<PhysicalCard, Map<PhysicalCard, SkirmishResult>> resultsIfAllPass = new HashMap<>();

            for (PhysicalCard companion : companions) {
                resultsIfAllPass.put(companion, new HashMap<>());
                for (PhysicalCard minion : minionsToAssign) {
                    // Check what happens if we assign this companion to this minion
                    SimulatedGame simulationCopy = simulation.copy();
                    simulationCopy.assignAndMoveToSkirmishFpPlayer(companion, minion);

                    SimulatedGame startOfSkirmish = simulationCopy.copy();
                    SkirmishPrediction skirmishPrediction = new SkirmishPrediction(startOfSkirmish);

                    SkirmishResult resultIfAllPass = skirmishPrediction.getResultIfAllPass();
                    resultsIfAllPass.get(companion).put(minion, resultIfAllPass);
                }
            }

            // Find the best assignment if nothing gets played
            Map<PhysicalCard, PhysicalCard> bestAssignments = new HashMap<>();
            int bestScore = Integer.MIN_VALUE;

            for (Map<PhysicalCard, PhysicalCard> assignment : generateAllAssignments(companions, minionsToAssign)) {
                int score = 0;
                for (Map.Entry<PhysicalCard, PhysicalCard> entry : assignment.entrySet()) {
                    PhysicalCard companion = entry.getKey();
                    PhysicalCard minion = entry.getValue();
                    SkirmishResult resultIfAllPass = resultsIfAllPass.get(companion).get(minion);
                    score += resultIfAllPass.score();
                }
                if (score > bestScore) {
                    bestScore = score;
                    bestAssignments = assignment;
                }
            }

            Plan plan = getAssignmentPlan(bestAssignments, minionsToLeaveUnassigned, assignMinionsDecisionRequest);
            BotLogging.log(REASONING, "Choosing assignment that is most favorable if nothing gets played");
            BotLogging.log(DEBUG, "Fp assignment plan:\n" + plan);
            return plan;
        }
        return null;
    }

    private static Plan getAssignmentPlan(Map<PhysicalCard, PhysicalCard> bestAssignments, List<PhysicalCard> minionsToLeaveUnassigned, AssignMinionsDecisionRequest assignMinionsDecisionRequest) {
        while (assignMinionsDecisionRequest.getNextMinion() != null) {
            PhysicalCard minion = assignMinionsDecisionRequest.getNextMinion();
            List<Option> options = assignMinionsDecisionRequest.getOptions();

            if (minionsToLeaveUnassigned.stream().anyMatch(c -> c.getCardId() == minion.getCardId())) {
                assignMinionsDecisionRequest.assign(findDoNotAssignOption(options));
            } else {
                PhysicalCard companionToAssign = bestAssignments.entrySet().stream()
                        .filter(entry -> entry.getValue().getCardId() == minion.getCardId())
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .orElse(null);
                if (companionToAssign != null) {
                    assignMinionsDecisionRequest.assign(findAssignToFpCharacter(options, companionToAssign));
                } else {
                    for (Map.Entry<PhysicalCard, PhysicalCard> physicalCardPhysicalCardEntry : bestAssignments.entrySet()) {
                        PhysicalCard companion = physicalCardPhysicalCardEntry.getKey();
                        PhysicalCard assignedMinion = physicalCardPhysicalCardEntry.getValue();
                        System.out.println(companion + "(" + companion.getCardId() + ")" + " -> " + assignedMinion.getBlueprint().getFullName() + "(" + assignedMinion.getCardId() + ")");
                    }
                    throw new IllegalStateException("No assignment found for minion: " + minion.getBlueprint().getFullName());
                }
            }
        }
        return new AssignmentSkirmishPlan(List.of(assignMinionsDecisionRequest.getChosenOptions()));
    }

    public static List<Map<PhysicalCard, PhysicalCard>> generateAllAssignments(Collection<PhysicalCard> companions, Collection<PhysicalCard> minions) {
        List<Map<PhysicalCard, PhysicalCard>> assignments = new ArrayList<>();

        generateAssignments(
                new ArrayList<>(companions),
                new ArrayList<>(minions),
                0,
                new HashSet<>(),
                new HashMap<>(),
                assignments
        );

        return assignments;
    }

    private static void generateAssignments(List<PhysicalCard> companions, List<PhysicalCard> minions, int minionIndex, Set<PhysicalCard> usedCompanions, Map<PhysicalCard, PhysicalCard> currentAssignment, List<Map<PhysicalCard, PhysicalCard>> assignments) {
        if (minionIndex == minions.size()) {
            assignments.add(new HashMap<>(currentAssignment));
            return;
        }

        PhysicalCard minion = minions.get(minionIndex);

        for (PhysicalCard companion : companions) {
            if (usedCompanions.contains(companion)) {
                continue;
            }

            currentAssignment.put(companion, minion);
            usedCompanions.add(companion);

            generateAssignments(companions, minions, minionIndex + 1, usedCompanions, currentAssignment, assignments);

            currentAssignment.remove(companion);
            usedCompanions.remove(companion);
        }
    }

}