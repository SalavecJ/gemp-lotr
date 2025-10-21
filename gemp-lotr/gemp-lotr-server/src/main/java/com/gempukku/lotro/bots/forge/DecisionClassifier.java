package com.gempukku.lotro.bots.forge;

import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;

import java.util.*;
import java.util.stream.Stream;

public class DecisionClassifier {

    public static boolean isBurdenBidDecision(AwaitingDecision awaitingDecision) {
        return awaitingDecision.getDecisionType().equals(AwaitingDecisionType.INTEGER)
                && awaitingDecision.getText().equals("Choose a number of burdens to bid");
    }

    public static boolean isGoFirstDecision(AwaitingDecision awaitingDecision) {
        if (!awaitingDecision.getDecisionType().equals(AwaitingDecisionType.MULTIPLE_CHOICE))
            return false;

        String[] options = awaitingDecision.getDecisionParameters().get("results");
        if (options.length != 2)
            return false;

        List<String> inputOptions = Arrays.stream(options).map(String::trim).toList();
        List<String> normalizedCardOptions = Stream.of("Go first", "Go second").map(String::trim).toList();

        return new HashSet<>(inputOptions).equals(new HashSet<>(normalizedCardOptions));
    }

    public static boolean isMulliganDecision(AwaitingDecision awaitingDecision) {
        return awaitingDecision.getDecisionType().equals(AwaitingDecisionType.MULTIPLE_CHOICE)
                && awaitingDecision.getText().equals("Do you wish to mulligan? (Shuffle cards back and draw 6)");
    }

    public static boolean isStartingFellowshipDecision(AwaitingDecision awaitingDecision) {
        return awaitingDecision.getDecisionType().equals(AwaitingDecisionType.ARBITRARY_CARDS)
                && awaitingDecision.getText().equals("Starting fellowship - Choose next character or press DONE");
    }

    public static boolean isDegenerateDecision(AwaitingDecision awaitingDecision) {
        if (awaitingDecision.getDecisionType().equals(AwaitingDecisionType.ARBITRARY_CARDS)) {
            Map<String, String[]> params = awaitingDecision.getDecisionParameters();
            String[] cardIds = params.get("cardId");
            String[] selectable = params.get("selectable");
            int min = params.containsKey("min") ? Integer.parseInt(params.get("min")[0]) : 0;
            int max = params.containsKey("max") ? Integer.parseInt(params.get("max")[0]) : cardIds.length;

            // Check if something is selectable
            if (cardIds == null || selectable == null || cardIds.length != selectable.length)
                return true; // Invalid input
            // Collect all selectable indices
            List<Integer> selectableIndices = new ArrayList<>();
            for (int i = 0; i < selectable.length; i++) {
                if (Boolean.parseBoolean(selectable[i])) {
                    selectableIndices.add(i);
                }
            }
            // If nothing is selectable or max == 0, the decision is just pass; or we need to select all
            return selectableIndices.isEmpty() || max == 0 || (min == max && max == selectableIndices.size());
        } else if (awaitingDecision.getDecisionType().equals(AwaitingDecisionType.CARD_SELECTION)) {
            int min = Integer.parseInt(awaitingDecision.getDecisionParameters().get("min")[0]);
            int max = Integer.parseInt(awaitingDecision.getDecisionParameters().get("max")[0]);
            List<String> cardIds = Arrays.stream(awaitingDecision.getDecisionParameters().get("cardId")).toList();

            return (min == max && min == cardIds.size()) || max == 0;
        } else if (awaitingDecision.getDecisionType().equals(AwaitingDecisionType.CARD_ACTION_CHOICE)) {
            // No action can be made
            return awaitingDecision.getDecisionParameters().get("cardId").length == 0;
        }
        return false;
    }

    public static boolean isAssignmentDecision(AwaitingDecision awaitingDecision) {
        return awaitingDecision.getDecisionType().equals(AwaitingDecisionType.ASSIGN_MINIONS);
    }

    public static boolean isMoveAgainDecision(AwaitingDecision awaitingDecision) {
        return awaitingDecision.getDecisionType().equals(AwaitingDecisionType.MULTIPLE_CHOICE)
                && awaitingDecision.getText().equals("Do you want to make another move?");
    }

    public static boolean isAssignArcheryShadowPlayerDecision(AwaitingDecision awaitingDecision) {
        return awaitingDecision.getDecisionType().equals(AwaitingDecisionType.CARD_SELECTION)
                && awaitingDecision.getText().contains("Choose minion to assign archery wound to - remaining wounds:");
    }

    public static boolean isAssignArcheryFpPlayerDecision(AwaitingDecision awaitingDecision) {
        return awaitingDecision.getDecisionType().equals(AwaitingDecisionType.CARD_SELECTION)
                && awaitingDecision.getText().contains("Choose character to assign archery wound to - remaining wounds:");
    }

    public static boolean isSanctuaryHealingDecision(AwaitingDecision awaitingDecision) {
        return awaitingDecision.getDecisionType().equals(AwaitingDecisionType.CARD_SELECTION)
                && awaitingDecision.getText().contains("Sanctuary healing - Choose companion to heal - remaining heals:");
    }

    public static boolean isOptionalReconcileDecision(AwaitingDecision awaitingDecision) {
        return awaitingDecision.getDecisionType().equals(AwaitingDecisionType.CARD_SELECTION)
                && awaitingDecision.getText().contains("Reconcile - choose card to discard or press DONE");
    }

    public static boolean isMandatoryReconcileDiscardDecision(AwaitingDecision awaitingDecision) {
        return awaitingDecision.getDecisionType().equals(AwaitingDecisionType.CARD_SELECTION)
                && awaitingDecision.getText().contains("Choose cards to discard down to 8");
    }

    public static boolean isChooseSkirmishOrderDecision(AwaitingDecision awaitingDecision) {
        return awaitingDecision.getDecisionType().equals(AwaitingDecisionType.CARD_SELECTION)
                && awaitingDecision.getText().equals("Choose next skirmish to resolve");
    }

    public static boolean isCardTargetingDecision(AwaitingDecision awaitingDecision) {
        return awaitingDecision.getDecisionType().equals(AwaitingDecisionType.CARD_SELECTION)
                && Integer.parseInt(awaitingDecision.getDecisionParameters().get(("source"))[0]) != -1;
    }

    public static boolean isArbitraryCardTargetingDecision(AwaitingDecision awaitingDecision) {
        return awaitingDecision.getDecisionType().equals(AwaitingDecisionType.ARBITRARY_CARDS)
                && Integer.parseInt(awaitingDecision.getDecisionParameters().get(("source"))[0]) != -1;
    }

    public static boolean isCardMultipleChoiceDecision(AwaitingDecision awaitingDecision) {
        return awaitingDecision.getDecisionType().equals(AwaitingDecisionType.MULTIPLE_CHOICE)
                && Integer.parseInt(awaitingDecision.getDecisionParameters().get(("source"))[0]) != -1;
    }

    public static boolean isCardIntegerChoiceDecision(AwaitingDecision awaitingDecision) {
        return awaitingDecision.getDecisionType().equals(AwaitingDecisionType.INTEGER)
                && Integer.parseInt(awaitingDecision.getDecisionParameters().get(("source"))[0]) != -1;
    }

}
