package com.gempukku.lotro.bots.forge.plan.action;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AssignMinionsAction extends ActionToTake {

    private final List<PhysicalCard> minionsToAssign;
    private final List<PhysicalCard> fpCharactersToAssignTo;
    private final boolean fpAssignment;

    private final DefaultLotroGame game;

    private final Map<PhysicalCard, List<PhysicalCard>> assignments = new HashMap<>();
    private final List<PhysicalCard> unassignedMinions;

    public static class SubAction {
        public final PhysicalCard minion;
        public PhysicalCard fpCharacter;

        public SubAction(PhysicalCard minion, PhysicalCard fpCharacter) {
            this.minion = minion;
            this.fpCharacter = fpCharacter;
        }

        @Override
        public String toString() {
            return "SubAction: Assign " + minion.getBlueprint().getFullName() + " to " + fpCharacter.getBlueprint().getFullName();
        }
    }

    public AssignMinionsAction(String decisionText, List<PhysicalCard> minionsToAssign, List<PhysicalCard> fpCharactersToAssignTo,
                                boolean fpAssignment, DefaultLotroGame game) {
        super(decisionText);
        this.minionsToAssign = minionsToAssign;
        this.fpCharactersToAssignTo = fpCharactersToAssignTo;
        this.fpAssignment = fpAssignment;
        this.unassignedMinions = new ArrayList<>(minionsToAssign);
        this.game = game;
    }

    public boolean isComplete() {
        if (fpAssignment) {
            return unassignedMinions.isEmpty() || fpCharactersToAssignTo.size() == assignments.size();
        } else {
            return unassignedMinions.isEmpty();
        }
    }

    public List<SubAction> getAvailableActions() {
        if (isComplete()) {
            throw new IllegalStateException("Assignment action is already complete");
        }
        List<SubAction> result = new ArrayList<>();
        PhysicalCard minion = unassignedMinions.stream().max((o1, o2) -> Integer.compare(game.getModifiersQuerying().getStrength(game, o1), game.getModifiersQuerying().getStrength(game, o2))).orElseThrow();
        if (fpAssignment) {
            List<PhysicalCard> freeFpCharacters = new ArrayList<>(fpCharactersToAssignTo);
            for (PhysicalCard assignedFpCharacter : assignments.keySet()) {
                freeFpCharacters.remove(assignedFpCharacter);
            }
            for (PhysicalCard fpCharacter : freeFpCharacters) {
                result.add(new SubAction(minion, fpCharacter));
            }
        } else {
            for (PhysicalCard fpCharacter : fpCharactersToAssignTo) {
                result.add(new SubAction(minion, fpCharacter));
            }
        }
        return result;
    }

    public void assign(SubAction action) {
        if (isComplete()) {
            throw new IllegalStateException("Assignment action is already complete");
        }
        if (!unassignedMinions.contains(action.minion)) {
            throw new IllegalArgumentException("Minion " + action.minion.getCardId() + " is already assigned");
        }
        assignments.computeIfAbsent(action.fpCharacter, k -> new ArrayList<>()).add(action.minion);
        unassignedMinions.remove(action.minion);
    }

    @Override
    public String carryOut() {
        if (!isComplete()) {
            throw new IllegalStateException("Assignment action not complete");
        }

        return assignments.entrySet().stream()
                .map((Function<Map.Entry<PhysicalCard, List<PhysicalCard>>, Map.Entry<String, List<String>>>) physicalCardListEntry -> new AbstractMap.SimpleEntry<>(
                        String.valueOf(physicalCardListEntry.getKey().getCardId()),
                        physicalCardListEntry.getValue().stream()
                                .map(card -> String.valueOf(card.getCardId()))
                                .collect(Collectors.toList())))
                .map(entry -> entry.getKey() + " " + String.join(" ", entry.getValue()))
                .collect(Collectors.joining(","));
    }

    @Override
    public String toString() {
        if (!isComplete()) {
            throw new IllegalStateException("Assignment action not complete");
        }
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<PhysicalCard, List<PhysicalCard>> entry : assignments.entrySet()) {
            builder.append("[");
            builder.append(entry.getKey().getBlueprint().getFullName());
            builder.append(": ");
            builder.append(entry.getValue().stream().map(physicalCard -> physicalCard.getBlueprint().getFullName()).collect(Collectors.joining(", ")));
            builder.append("] ");
        }
        if (!unassignedMinions.isEmpty()) {
            builder.append("[Unassigned: ");
            builder.append(unassignedMinions.stream().map(physicalCard -> physicalCard.getBlueprint().getFullName()).collect(Collectors.joining(", ")));
            builder.append("]");
        }

        return "Action: Assign Minions - " + builder;
    }
}
