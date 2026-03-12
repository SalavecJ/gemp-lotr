package com.gempukku.lotro.bots.forge.plan.action;

import com.gempukku.lotro.bots.forge.cards.abstractcards.BotCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AssignMinionsAction extends ActionToTake {

    private final List<BotCard> minionsToAssign;
    private final List<BotCard> fpCharactersToAssignTo;
    private final boolean fpAssignment;

    private final DefaultLotroGame game;

    private final Map<BotCard, List<BotCard>> assignments = new HashMap<>();
    private final List<BotCard> unassignedMinions;

    public static class SubAction {
        public final BotCard minion;
        public BotCard fpCharacter;

        public SubAction(BotCard minion, BotCard fpCharacter) {
            this.minion = minion;
            this.fpCharacter = fpCharacter;
        }

        @Override
        public String toString() {
            return "SubAction: Assign " + minion.getFullName() + " to " + fpCharacter.getFullName();
        }
    }

    public AssignMinionsAction(String decisionText, List<BotCard> minionsToAssign, List<BotCard> fpCharactersToAssignTo,
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
        for (BotCard minion : unassignedMinions) {
            if (fpAssignment) {
                List<BotCard> freeFpCharacters = new ArrayList<>(fpCharactersToAssignTo);
                for (BotCard assignedFpCharacter : assignments.keySet()) {
                    freeFpCharacters.remove(assignedFpCharacter);
                }
                for (BotCard fpCharacter : freeFpCharacters) {
                    result.add(new SubAction(minion, fpCharacter));
                }
            } else {
                for (BotCard fpCharacter : fpCharactersToAssignTo) {
                    result.add(new SubAction(minion, fpCharacter));
                }
            }
        }
        return result;
    }

    public void assign(SubAction action) {
        if (isComplete()) {
            throw new IllegalStateException("Assignment action is already complete");
        }
        if (!unassignedMinions.contains(action.minion)) {
            throw new IllegalArgumentException("Minion " + action.minion.getPhysicalCard().getCardId() + " is already assigned");
        }
        assignments.computeIfAbsent(action.fpCharacter, k -> new ArrayList<>()).add(action.minion);
        unassignedMinions.remove(action.minion);
    }

    public List<BotCard> getMinionsToAssign() {
        return minionsToAssign;
    }

    public List<BotCard> getFpCharactersToAssignTo() {
        return fpCharactersToAssignTo;
    }

    @Override
    public String carryOut() {
        if (!isComplete()) {
            throw new IllegalStateException("Assignment action not complete");
        }

        return assignments.entrySet().stream()
                .map((Function<Map.Entry<BotCard, List<BotCard>>, Map.Entry<String, List<String>>>) BotCardListEntry -> new AbstractMap.SimpleEntry<>(
                        String.valueOf(BotCardListEntry.getKey().getPhysicalCard().getCardId()),
                        BotCardListEntry.getValue().stream()
                                .map(card -> String.valueOf(card.getPhysicalCard().getCardId()))
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
        for (Map.Entry<BotCard, List<BotCard>> entry : assignments.entrySet()) {
            builder.append("[");
            builder.append(entry.getKey().getFullName());
            builder.append(": ");
            builder.append(entry.getValue().stream().map(BotCard::getFullName).collect(Collectors.joining(", ")));
            builder.append("] ");
        }
        if (!unassignedMinions.isEmpty()) {
            builder.append("[Unassigned: ");
            builder.append(unassignedMinions.stream().map(BotCard::getFullName).collect(Collectors.joining(", ")));
            builder.append("]");
        }

        return "Action: Assign Minions - " + builder;
    }
}
