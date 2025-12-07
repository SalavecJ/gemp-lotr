package com.gempukku.lotro.bots.forge.utils;

import com.gempukku.lotro.bots.forge.cards.BotCardFactory;
import com.gempukku.lotro.bots.forge.cards.abstractcard.BotCard;
import com.gempukku.lotro.bots.forge.plan.action2.*;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.decisions.*;
import com.gempukku.lotro.logic.modifiers.ModifierFlag;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DecisionToActions {
    private static final String PLAY_ACTION_PREFIX = "Play ";
    private static final String USE_ACTION_PREFIX = "Use ";
    private static final String TRANSFER_ACTION_PREFIX = "Transfer ";
    private static final String OPTIONAL_TRIGGER_PREFIX = "Optional trigger ";
    private static final String OPTIONAL_IS_ABOUT_TO_RESPONSES = "Optional \\\"is about to\\\" responses";
    private static final String ARCHERY_FIRE_FP_PREFIX = "Choose character to assign archery wound to - remaining wounds: ";
    private static final String ARCHERY_FIRE_SHADOW_PREFIX = "Choose minion to assign archery wound to - remaining wounds: ";
    private static final String HEAL_BY_DISCARDING = "Heal by discarding";
    private static final String CHOOSE_NEXT_SKIRMISH = "Choose next skirmish to resolve";
    private static final String REQUIRED_RESPONSES = "Required responses";

    public static List<ActionToTake2> toActions(AwaitingDecision decision, DefaultLotroGame game) {
//        System.out.println("Converting decision to actions: " + decision.toJson());
        List<ActionToTake2> tbr = new ArrayList<>();

        switch (decision) {
            case CardActionSelectionDecision cardActionSelectionDecision -> {
                List<String> actionIds = Arrays.asList(decision.getDecisionParameters().get("actionId"));
                List<String> actionTexts = Arrays.asList(decision.getDecisionParameters().get("actionText"));
                List<String> physicalIds = Arrays.asList(decision.getDecisionParameters().get("cardId"));
                if (!actionTexts.isEmpty() && actionTexts.stream().allMatch(s -> s.startsWith(OPTIONAL_TRIGGER_PREFIX))) {
                    // Optional triggers
                    for (int i = 0; i < actionIds.size(); i++) {
                        String actionId = actionIds.get(i);
                        int physicalId = Integer.parseInt(physicalIds.get(i));

                        BotCard botCard = BotCardFactory.create(game.getGameState().findCardById(physicalId));
                        tbr.add(new AcceptTriggerAction2(decision.getText(), botCard, actionId));
                    }
                    tbr.add(new DenyTriggerAction2(decision.getText()));
                } else if (decision.getText().contains(OPTIONAL_IS_ABOUT_TO_RESPONSES)) {
                    for (int i = 0; i < actionIds.size(); i++) {
                        String actionId = actionIds.get(i);
                        String actionText = actionTexts.get(i);
                        int physicalId = Integer.parseInt(physicalIds.get(i));

                        BotCard botCard = BotCardFactory.create(game.getGameState().findCardById(physicalId));
                        if (actionText.equals(PLAY_ACTION_PREFIX + botCard.getSelf().getBlueprint().getFullName())) {
                            tbr.add(new PlayCardFromHandAction2(decision.getText(), botCard, actionId));
                        } else if (actionText.equals(USE_ACTION_PREFIX + botCard.getSelf().getBlueprint().getFullName())) {
                            if (botCard.getSelf().getBlueprint().getFullName().equals("The One Ring, The Ruling Ring")
                                    && game.getModifiersQuerying().getVitality(game, game.getGameState().getRingBearer(game.getGameState().getCurrentPlayerId())) > 1) {
                                // Do not offer to use The Ruling Ring if RB is not exhausted
                                continue;
                            }
                            tbr.add(new UseCardAction2(decision.getText(), botCard, actionId));
                        } else {
                            throw new IllegalStateException("Unknown action text: " + actionText);
                        }
                    }
                    tbr.add(new PassAction2(decision.getText()));
                } else {
                    //Assume nothing can be used and play maneuver to regroup
                    if (decision.getText().equals("Play Maneuver action or Pass") ||
                            decision.getText().equals("Play Archery action or Pass") ||
                            decision.getText().equals("Play Assignment action or Pass") ||
                            decision.getText().equals("Choose action to play or Pass") ||
                            decision.getText().equals("Play Regroup action or Pass")) {
                        return List.of(new PassAction2(decision.getText()));
                    }

                    for (int i = 0; i < actionIds.size(); i++) {
                        String actionId = actionIds.get(i);
                        String actionText = actionTexts.get(i);
                        int physicalId = Integer.parseInt(physicalIds.get(i));

                        BotCard botCard = BotCardFactory.create(game.getGameState().findCardById(physicalId));
                        if (actionText.equals(PLAY_ACTION_PREFIX + botCard.getSelf().getBlueprint().getFullName())) {
                            tbr.add(new PlayCardFromHandAction2(decision.getText(), botCard, actionId));
                        } else if (actionText.equals(USE_ACTION_PREFIX + botCard.getSelf().getBlueprint().getFullName())) {
                            if (botCard.getSelf().getBlueprint().getFullName().equals("The Bridge of Khazad-dûm")) {
                                // Do not offer to use The Bridge of Khazad-dûm
                                continue;
                            }
                            if (botCard.getSelf().getBlueprint().getFullName().equals("Shores of Nen Hithoel") && game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.CANT_MOVE)) {
                                // Do not offer to use Shores of Nen Hithoel if FP cannot move to prevent loop
                                continue;
                            }
                            tbr.add(new UseCardAction2(decision.getText(), botCard, actionId));
                        } else if (actionText.equals(HEAL_BY_DISCARDING)) {
                            tbr.add(new DiscardCompanionToHealAction2(decision.getText(), botCard, actionId));
                        } else if (actionText.startsWith(TRANSFER_ACTION_PREFIX)) {
                            // Do not offer transfer actions
                        } else {
                            throw new IllegalStateException("Unknown action text: " + actionText);
                        }
                    }
                    tbr.add(new PassAction2(decision.getText()));
                }
            }
            case CardsSelectionDecision cardsSelectionDecision -> {
                int min = Integer.parseInt(decision.getDecisionParameters().get("min")[0]);
                int max = Integer.parseInt(decision.getDecisionParameters().get("max")[0]);
                List<String> physicalIds = Arrays.asList(decision.getDecisionParameters().get("cardId"));
                int sourceId = Integer.parseInt(decision.getDecisionParameters().get("source")[0]);
                if (sourceId == -1) {
                    // No source card
                    if (decision.getText().equals(CHOOSE_NEXT_SKIRMISH)) {
                        for (String physicalId : physicalIds) {
                            PhysicalCard fpCharacter = game.getGameState().findCardById(Integer.parseInt(physicalId));
                            tbr.add(new ChooseSkirmishAction2(decision.getText(), fpCharacter));
                        }
                    } else if (decision.getText().startsWith(ARCHERY_FIRE_FP_PREFIX) || decision.getText().startsWith(ARCHERY_FIRE_SHADOW_PREFIX)) {
                        for (String physicalId : physicalIds) {
                            PhysicalCard card = game.getGameState().findCardById(Integer.parseInt(physicalId));
                            tbr.add(new AssignArcheryWoundAction2(decision.getText(), card));
                        }
                    } else {
                        throw new IllegalStateException("Only skirmish selection without source card is supported, but got decision text: " + decision.getText());
                    }
                } else {
                    BotCard sourceCard = BotCardFactory.create(game.getGameState().findCardById(sourceId));
                    if (min != max) {
                        throw new IllegalStateException("Only fixed card selection is supported (min must equal max), but got min: " + min + ", max: " + max);
                    }

                    // Convert physical IDs to BotCards
                    List<BotCard> availableTargets = new ArrayList<>();
                    for (String physicalId : physicalIds) {
                        availableTargets.add(BotCardFactory.create(game.getGameState().findCardById(Integer.parseInt(physicalId))));
                    }

                    // Generate all combinations of size 'min' (which equals 'max')
                    List<List<BotCard>> combinations = generateCombinations(availableTargets, min);

                    // Create an action for each combination
                    for (List<BotCard> combination : combinations) {
                        tbr.add(new ChooseTargetsAction2(decision.getText(), sourceCard, combination));
                    }
                }
            }
            case ArbitraryCardsSelectionDecision arbitraryCardsSelectionDecision -> {
                int min = Integer.parseInt(decision.getDecisionParameters().get("min")[0]);
                int max = Integer.parseInt(decision.getDecisionParameters().get("max")[0]);
                List<String> tempIds = Arrays.asList(decision.getDecisionParameters().get("cardId"));
                List<String> physicalIds = Arrays.asList(decision.getDecisionParameters().get("physicalId"));
                List<String> selectable = Arrays.asList(decision.getDecisionParameters().get("selectable"));
                int sourceId = Integer.parseInt(decision.getDecisionParameters().get("source")[0]);

                BotCard sourceCard = BotCardFactory.create(game.getGameState().findCardById(sourceId));
                if (min == 0 && max == 0) {
                    return List.of(new PassAction2(decision.getText()));
                }
                if (min != 1 || max != 1) {
                    throw new IllegalStateException("Only single card selection is supported, but got min: " + min + ", max: " + max);
                }
                for (int i = 0; i < physicalIds.size(); i++) {
                    if (Boolean.parseBoolean(selectable.get(i))) {
                        BotCard targetCard = BotCardFactory.create(game.getGameState().findCardById(Integer.parseInt(physicalIds.get(i))));
                        tbr.add(new ChooseArbitraryTargetsAction2(decision.getText(), sourceCard, List.of(targetCard), List.of(tempIds.get(i))));
                    }
                }

            }
            case PlayerAssignMinionsDecision playerAssignMinionsDecision -> {
                List<String> minionIds = Arrays.asList(decision.getDecisionParameters().get("minions"));
                List<PhysicalCard> minions = minionIds.stream().map(s -> {
                    int physicalId = Integer.parseInt(s);
                    return game.getGameState().findCardById(physicalId);
                }).toList();

                List<String> freeCharacterIds = Arrays.asList(decision.getDecisionParameters().get("freeCharacters"));
                List<PhysicalCard> freeCharacters = freeCharacterIds.stream().map(s -> {
                    int physicalId = Integer.parseInt(s);
                    return game.getGameState().findCardById(physicalId);
                }).toList();

                boolean fpAssignment = decision.getDecisionParameters().get("player")[0].equals("fp");

                return List.of(new AssignMinionsAction2(decision.getText(), minions, freeCharacters, fpAssignment, game));
            }
            case ActionSelectionDecision actionSelectionDecision -> {
                if (decision.getText().equals(REQUIRED_RESPONSES)) {
                    List<String> actionIds = Arrays.asList(decision.getDecisionParameters().get("actionId"));
                    List<String> actionTexts = Arrays.asList(decision.getDecisionParameters().get("actionText"));
//                for (int i = 0; i < actionIds.size(); i++) {
                    for (int i = 0; i < 1; i++) { // Whatever order, minimalize branching
                        String actionId = actionIds.get(i);
                        String actionText = actionTexts.get(i);

                        tbr.add(new AcceptRequiredResponseAction2(decision.getText(), Integer.parseInt(actionId), actionText));
                    }
                } else {
                    throw new IllegalStateException("Unknown action selection decision: " + decision.toJson());
                }
            }
            default -> {
                System.out.println("Unknown decision: " + decision.toJson());
                throw new IllegalStateException("Unknown decision type: " + decision.getClass());
            }
        }

        return tbr;
    }

    /**
     * Generates all combinations of size k from the given list.
     * For example, if list = [A, B, C] and k = 2, returns [[A, B], [A, C], [B, C]]
     */
    private static <T> List<List<T>> generateCombinations(List<T> list, int k) {
        List<List<T>> result = new ArrayList<>();

        if (k == 0) {
            result.add(new ArrayList<>());
            return result;
        }

        if (k > list.size()) {
            return result; // No combinations possible
        }

        generateCombinationsHelper(list, k, 0, new ArrayList<>(), result);
        return result;
    }

    /**
     * Recursive helper method for generating combinations.
     */
    private static <T> void generateCombinationsHelper(List<T> list, int k, int start, List<T> current, List<List<T>> result) {
        if (current.size() == k) {
            result.add(new ArrayList<>(current));
            return;
        }

        for (int i = start; i < list.size(); i++) {
            current.add(list.get(i));
            generateCombinationsHelper(list, k, i + 1, current, result);
            current.removeLast();
        }
    }
}
