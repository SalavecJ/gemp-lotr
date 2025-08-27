package com.gempukku.lotro.bots.forge.controller;

import com.gempukku.lotro.bots.forge.utils.CombatUtil;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.effects.ResolveSkirmishEffect;

import java.util.*;

public class AiAssignmentController {
    private final boolean printDebugMessages;

    private final LotroGame game;
    private final boolean isFpAssignment;

    private final List<PhysicalCard> fpCards = new ArrayList<>();
    private final List<PhysicalCard> minionCards = new ArrayList<>();
    private final Map<PhysicalCard, Set<PhysicalCard>> alreadyAssignedMap = new HashMap<>();

    private final List<Map<PhysicalCard, Set<PhysicalCard>>> possibleAssignments = new ArrayList<>();


    public AiAssignmentController(String aiPlayerName, boolean printDebugMessages, LotroGame game, AwaitingDecision awaitingDecision) {
        this.printDebugMessages = printDebugMessages;

        this.game = game;

        this.isFpAssignment = game.getGameState().getCurrentPlayerId().equals(aiPlayerName);


        Map<String, String[]> params = awaitingDecision.getDecisionParameters();

        for (String freeCharId : params.get("freeCharacters")) {
            fpCards.add(game.getGameState().getPhysicalCard(Integer.parseInt(freeCharId)));
        }
        for (String minionId : params.get("minions")) {
            minionCards.add(game.getGameState().getPhysicalCard(Integer.parseInt(minionId)));
        }

        game.getGameState().getAssignments().forEach(assignment ->
                alreadyAssignedMap.put(assignment.getFellowshipCharacter(), assignment.getShadowCharacters()));
    }

    public Map<String, List<String>> chooseAssignment() {
        if (printDebugMessages) {
            System.out.println("Making assignment as " + (isFpAssignment ? "fp" : "shadow") + " player");
            StringBuilder fp = new StringBuilder();
            fp.append("Fp cards: ");
            for (PhysicalCard fpCard : fpCards) {
                fp.append(fpCard.getBlueprint().getFullName()).append("; ");
            }
            System.out.println(fp);
            StringBuilder shadow = new StringBuilder();
            shadow.append("Shadow cards: ");
            for (PhysicalCard minionCard : minionCards) {
                shadow.append(minionCard.getBlueprint().getFullName()).append("; ");
            }
            System.out.println(shadow);
        }

        // Remove cards that cannot be assigned by player
        minionCards.removeIf(minionCard -> {
            boolean canBeAssigned = fpCards.stream().anyMatch(companionCard -> game.getModifiersQuerying().isValidAssignments(game, isFpAssignment ? Side.FREE_PEOPLE : Side.SHADOW, Map.of(companionCard, Set.of(minionCard))));
            if (printDebugMessages && !canBeAssigned) {
                System.out.println("Removing minion that cannot be assigned: " + minionCard.getBlueprint().getFullName());
            }

            return !canBeAssigned;
        });

        return isFpAssignment ? chooseAssignmentAsFp() : chooseAssignmentAsShadow();
    }

    private Map<String, List<String>> chooseAssignmentAsShadow() {
        // Generate all possible assignments
        generateShadowAssignmentsRecursive(0, alreadyAssignedMap);
        if (printDebugMessages) {
            System.out.println("Possible assignments generated: " + possibleAssignments.size());
        }

        // Score assignments
        Map<Map<PhysicalCard, Set<PhysicalCard>>, Integer> scoredAssignments = new HashMap<>();
        for (Map<PhysicalCard, Set<PhysicalCard>> possibleAssignment : possibleAssignments) {
            Set<PhysicalCard> unassigned = new HashSet<>(minionCards);
            for (Set<PhysicalCard> minions : possibleAssignment.values()) {
                unassigned.removeAll(minions);
            }
            scoredAssignments.put(possibleAssignment, scoreAssignment(possibleAssignment, unassigned));
        }

        // Pick the 'worst' one
        int worstScore = scoredAssignments.values().stream().max((o1, o2) -> -Integer.compare(o1, o2)).orElseThrow();
        Map<PhysicalCard, Set<PhysicalCard>> chosenAssignment = scoredAssignments.entrySet().stream().filter(mapIntegerEntry -> mapIntegerEntry.getValue() == worstScore).map(Map.Entry::getKey).findFirst().orElseThrow();

        if (printDebugMessages) {
            System.out.println("Worst assignment score: " + worstScore);
            // Print the chosen assignment
            for (Map.Entry<PhysicalCard, Set<PhysicalCard>> physicalCardSetEntry : chosenAssignment.entrySet()) {
                StringBuilder builder = new StringBuilder();
                builder.append(physicalCardSetEntry.getKey().getBlueprint().getFullName()).append(" -");
                for (PhysicalCard minion : physicalCardSetEntry.getValue()) {
                    builder.append(" ").append(minion.getBlueprint().getFullName()).append(";");
                }
                System.out.println(builder);
            }
        }

        // Remove the previously assigned pairs from the decision
        for (Map.Entry<PhysicalCard, Set<PhysicalCard>> alreadyAssigned : alreadyAssignedMap.entrySet()) {
            chosenAssignment.get(alreadyAssigned.getKey()).removeAll(alreadyAssigned.getValue());
        }

        // Convert physical cards to ids
        Map<String, List<String>> tbr = new HashMap<>();
        for (Map.Entry<PhysicalCard, Set<PhysicalCard>> chosen : chosenAssignment.entrySet()) {
            tbr.put(String.valueOf(chosen.getKey().getCardId()), new ArrayList<>());
            for (PhysicalCard minion : chosen.getValue()) {
                tbr.get(String.valueOf(chosen.getKey().getCardId())).add(String.valueOf(minion.getCardId()));
            }
        }

        return tbr;
    }

    private Map<String, List<String>> chooseAssignmentAsFp() {
        // Generate all possible assignments
        generateFpAssignmentsRecursive(0, alreadyAssignedMap, new ArrayList<>());
        if (printDebugMessages) {
            System.out.println("Possible assignments generated: " + possibleAssignments.size());
        }

        // Score assignments
        Map<Map<PhysicalCard, Set<PhysicalCard>>, Integer> scoredAssignments = new HashMap<>();
        for (Map<PhysicalCard, Set<PhysicalCard>> possibleAssignment : possibleAssignments) {
            Set<PhysicalCard> unassigned = new HashSet<>(minionCards);
            for (Set<PhysicalCard> minions : possibleAssignment.values()) {
                unassigned.removeAll(minions);
            }
            // Rather assign than leave for shadow player if possible
            scoredAssignments.put(possibleAssignment, scoreAssignment(possibleAssignment, unassigned) - (unassigned.size() * 50));
        }

        // Pick the best assignment
        int bestScore = scoredAssignments.values().stream().max(Integer::compare).orElseThrow();
        Map<PhysicalCard, Set<PhysicalCard>> chosenAssignment = scoredAssignments.entrySet().stream().filter(mapIntegerEntry -> mapIntegerEntry.getValue() == bestScore).map(Map.Entry::getKey).findFirst().orElseThrow();

        if (printDebugMessages) {
            System.out.println("Best assignment score: " + bestScore);

            // Print unassigned minions
            Set<PhysicalCard> unassigned = new HashSet<>(minionCards);
            for (Set<PhysicalCard> minions : chosenAssignment.values()) {
                unassigned.removeAll(minions);
            }
            StringBuilder unassignedBuilder = new StringBuilder();
            unassignedBuilder.append("Unassigned -");
            for (PhysicalCard minion : unassigned) {
                unassignedBuilder.append(" ").append(minion.getBlueprint().getFullName()).append(";");
            }
            if (!unassigned.isEmpty()) {
                System.out.println(unassignedBuilder);
            }

            // Print the chosen assignment
            for (Map.Entry<PhysicalCard, Set<PhysicalCard>> physicalCardSetEntry : chosenAssignment.entrySet()) {
                StringBuilder builder = new StringBuilder();
                builder.append(physicalCardSetEntry.getKey().getBlueprint().getFullName()).append(" -");
                for (PhysicalCard minion : physicalCardSetEntry.getValue()) {
                    builder.append(" ").append(minion.getBlueprint().getFullName()).append(";");
                }
                System.out.println(builder);
            }
        }

        // Remove the previously assigned pairs from the decision
        for (Map.Entry<PhysicalCard, Set<PhysicalCard>> alreadyAssigned : alreadyAssignedMap.entrySet()) {
            chosenAssignment.get(alreadyAssigned.getKey()).removeAll(alreadyAssigned.getValue());
        }

        // Convert physical cards to ids
        Map<String, List<String>> tbr = new HashMap<>();
        for (Map.Entry<PhysicalCard, Set<PhysicalCard>> chosen : chosenAssignment.entrySet()) {
            tbr.put(String.valueOf(chosen.getKey().getCardId()), new ArrayList<>());
            for (PhysicalCard minion : chosen.getValue()) {
                tbr.get(String.valueOf(chosen.getKey().getCardId())).add(String.valueOf(minion.getCardId()));
            }
        }

        return tbr;
    }

    private void generateFpAssignmentsRecursive(int index,
                                                Map<PhysicalCard, Set<PhysicalCard>> current,
                                                List<PhysicalCard> unassignedMinions) {

        Map<PhysicalCard, Set<PhysicalCard>> copy = deepCopyAssignment(current);

        if (index >= minionCards.size()) {
            if (game.getModifiersQuerying().isValidAssignments(game, Side.FREE_PEOPLE, copy)) {
                possibleAssignments.add(copy);
            }
            return;
        }

        PhysicalCard minionCard = minionCards.get(index);

        for (PhysicalCard fpCard : fpCards) {
            boolean shouldRemove = !copy.containsKey(fpCard);
            copy.putIfAbsent(fpCard, new HashSet<>());
            // TODO defender
            if (copy.get(fpCard).isEmpty()) {
                copy.get(fpCard).add(minionCard);

                generateFpAssignmentsRecursive(index + 1, copy, unassignedMinions);

                // backtrack
                copy.get(fpCard).remove(minionCard);
            }
            if (shouldRemove) {
                copy.remove(fpCard);
            }
        }

        // Let the current minion remain unassigned
        unassignedMinions.add(minionCard);
        generateFpAssignmentsRecursive(index + 1, copy, unassignedMinions);
        unassignedMinions.removeLast(); // backtrack
    }

    private void generateShadowAssignmentsRecursive(int index,
                                                      Map<PhysicalCard, Set<PhysicalCard>> current) {
        Map<PhysicalCard, Set<PhysicalCard>> copy = deepCopyAssignment(current);
        if (index >= minionCards.size()) {
            if (game.getModifiersQuerying().isValidAssignments(game, Side.SHADOW, copy)) {
                possibleAssignments.add(copy);
            }
            return;
        }

        PhysicalCard minionCard = minionCards.get(index);

        for (PhysicalCard fpCard : fpCards) {
            boolean shouldRemove = !copy.containsKey(fpCard);

            copy.putIfAbsent(fpCard, new HashSet<>());
            copy.get(fpCard).add(minionCard);

            generateShadowAssignmentsRecursive(index + 1, copy);

            copy.get(fpCard).remove(minionCard); // backtrack

            if (shouldRemove) {
                copy.remove(fpCard);
            }
        }
    }

    private Map<PhysicalCard, Set<PhysicalCard>> deepCopyAssignment(Map<PhysicalCard, Set<PhysicalCard>> original) {
        Map<PhysicalCard, Set<PhysicalCard>> copy = new HashMap<>();
        for (Map.Entry<PhysicalCard, Set<PhysicalCard>> e : original.entrySet()) {
            copy.put(e.getKey(), new HashSet<>(e.getValue()));
        }
        return copy;
    }

    private int scoreAssignment(Map<PhysicalCard, Set<PhysicalCard>> current, Set<PhysicalCard> unassigned) {
        if (unassigned.isEmpty()) {
            // Everything is assigned
            return scoreFinalAssignment(current);
        } else {
            // Recursive case: try assigning the first unassigned minion to each FP card
            PhysicalCard minion = unassigned.iterator().next();
            Set<PhysicalCard> remaining = new HashSet<>(unassigned);
            remaining.remove(minion);

            int bestScore = Integer.MAX_VALUE; // Shadow wants lowest score

            for (PhysicalCard fpCard : fpCards) {
                Map<PhysicalCard, Set<PhysicalCard>> copy = deepCopyAssignment(current);

                // Assign this minion to this FP card
                copy.computeIfAbsent(fpCard, k -> new HashSet<>()).add(minion);

                // Recurse
                int score = scoreAssignment(copy, remaining);

                // Keep the lowest score
                if (score < bestScore) {
                    bestScore = score;
                }
            }
            return bestScore;
        }
    }

    private int scoreFinalAssignment(Map<PhysicalCard, Set<PhysicalCard>> current) {
        boolean ringbearerDies = false;
        boolean samCanTakeTheRing = fpCards.stream().anyMatch(physicalCard -> physicalCard.getBlueprint().getTitle().equals("Sam")
                && !physicalCard.getBlueprint().getSubtitle().equals("Bearer of Great Need")
                && !physicalCard.getBlueprint().getSubtitle().equals("Steadfast Friend"));
        int companionsKilled = 0;
        int companionsVitalityLost = 0;
        int alliesKilled = 0;
        int alliesVitalityLost = 0;
        int minionsKilled = 0;
        int minionsWoundsTaken = 0;

        for (Map.Entry<PhysicalCard, Set<PhysicalCard>> assignment : current.entrySet()) {
            PhysicalCard fp = assignment.getKey();
            Set<PhysicalCard> minions = assignment.getValue();
            // TODO potentially use cards
            ResolveSkirmishEffect.Result result = CombatUtil.getPotentialResultIfNothingUsed(game, fp, minions);

            if (result.equals(ResolveSkirmishEffect.Result.FELLOWSHIP_OVERWHELMED)) {
                if (fp.getBlueprint().getCardType().equals(CardType.COMPANION)) {
                    companionsKilled++;
                    companionsVitalityLost += game.getModifiersQuerying().getVitality(game, fp);
                } else if (fp.getBlueprint().getCardType().equals(CardType.ALLY)) {
                    alliesKilled++;
                    alliesVitalityLost += game.getModifiersQuerying().getVitality(game, fp);
                }
                if (game.getGameState().getRingBearers().contains(fp)) {
                    ringbearerDies = true;
                }
                if (fp.getBlueprint().getTitle().equals("Sam")) {
                    // TODO strength from ring
                    samCanTakeTheRing = false;
                }
            } else if (result.equals(ResolveSkirmishEffect.Result.FELLOWSHIP_LOSES)) {
                int woundsToTake = CombatUtil.getPotentialDamageTakenIfFellowshipLoses(game, fp, minions);
                // TODO if ringbearer, check if can get burdens instead
                boolean dies = woundsToTake >= game.getModifiersQuerying().getVitality(game, fp);

                if (fp.getBlueprint().getCardType().equals(CardType.COMPANION)) {
                    if (dies) {
                        companionsKilled++;
                        if (fp.getBlueprint().getTitle().equals("Sam")) {
                            // TODO strength and burdens from ring
                            samCanTakeTheRing = false;
                        }
                    }
                    companionsVitalityLost += Math.min(game.getModifiersQuerying().getVitality(game, fp), woundsToTake);
                } else if (fp.getBlueprint().getCardType().equals(CardType.ALLY)) {
                    if (dies)
                        alliesKilled++;
                    alliesVitalityLost += Math.min(game.getModifiersQuerying().getVitality(game, fp), woundsToTake);
                }

                if (game.getGameState().getRingBearers().contains(fp) && dies) {
                    ringbearerDies = true;
                }
            } else if (result.equals(ResolveSkirmishEffect.Result.SHADOW_OVERWHELMED)) {
                minionsKilled += minions.size();
            } else if (result.equals(ResolveSkirmishEffect.Result.SHADOW_LOSES)) {
                int woundsToTake = CombatUtil.getPotentialDamageTakenIfShadowLoses(game, fp);
                for (PhysicalCard minion : minions) {
                    if (woundsToTake >= game.getModifiersQuerying().getVitality(game, minion)) {
                        minionsKilled++;
                    } else {
                        minionsWoundsTaken += woundsToTake;
                    }
                }
            }
        }

        int tbr = 0;

        if (ringbearerDies && !samCanTakeTheRing) {
            tbr -= 1_000;
        }

        tbr -= 30 * companionsKilled;
        tbr -= 20 * companionsVitalityLost;

        tbr -= 20 * alliesKilled;
        tbr -= 5 * alliesVitalityLost;

        tbr += 10 * minionsKilled;
        tbr += 5 * minionsWoundsTaken;

        return tbr;
    }
}
