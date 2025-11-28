package com.gempukku.lotro.bots.forge;

import com.gempukku.lotro.bots.BotPlayer;
import com.gempukku.lotro.bots.random.RandomDecisionBot;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ForgeBot extends RandomDecisionBot implements BotPlayer {
    private final AiController brains;
    private final boolean printDebugMessages;

    public ForgeBot(String name) {
        this(name, false);
    }
    public ForgeBot(String name, boolean printDebugMessages) {
        super(name);
        if (!name.startsWith("~")) {
            throw new IllegalArgumentException("Bot names need to start with '~' character");
        }
        this.brains = new AiController(name, printDebugMessages);
        this.printDebugMessages = printDebugMessages;
    }

    @Override
    public void cleanUpAfterGame() {
        brains.cleanUpAfterGame();
    }

    @Override
    public String chooseAction(DefaultLotroGame game, AwaitingDecision awaitingDecision) {
        try {
            return makeDecision(game, awaitingDecision);
        } catch (UnsupportedOperationException e) {
            if (printDebugMessages) {
                System.out.println(e.getMessage());
            }
            return super.chooseAction(game, awaitingDecision);
        }
    }

    private String makeDecision(DefaultLotroGame game, AwaitingDecision awaitingDecision) {
        return switch (awaitingDecision.getDecisionType()) {
            case INTEGER -> makeIntegerDecision(game, awaitingDecision);
            case MULTIPLE_CHOICE -> makeMultipleChoiceDecision(game, awaitingDecision);
            case ARBITRARY_CARDS -> makeArbitraryCardsSelectionDecision(game, awaitingDecision);
            case CARD_ACTION_CHOICE -> makeCardActionChoiceDecision(game, awaitingDecision);
            case ACTION_CHOICE -> makeActionChoiceDecision(game, awaitingDecision);
            case CARD_SELECTION -> makeCardSelectionDecision(game, awaitingDecision);
            case ASSIGN_MINIONS -> makeAssignmentDecision(game, awaitingDecision);
        };
    }

    private String makeActionChoiceDecision(LotroGame game, AwaitingDecision awaitingDecision) {
        try {
            return String.valueOf(brains.chooseRequiredResponseToResolveNext(game, awaitingDecision));
        } catch (CardNotFoundException e) {
            throw new UnsupportedOperationException("Decision not supported: " + awaitingDecision.toJson().toString());
        }
    }

    private String makeCardActionChoiceDecision(DefaultLotroGame game, AwaitingDecision awaitingDecision) {
        if (DecisionClassifier.isDegenerateDecision(awaitingDecision)) {
            // No action can be taken, just pass
            return "";
        } else {
            try {
                return brains.chooseActionToTakeNext(game, awaitingDecision);
            } catch (CardNotFoundException e) {
                throw new UnsupportedOperationException("Decision not supported: " + awaitingDecision.toJson().toString());
            }
        }
    }

    private String makeAssignmentDecision(LotroGame game, AwaitingDecision awaitingDecision) {
        if (DecisionClassifier.isAssignmentDecision(awaitingDecision)) {
            Map<String, List<String>> assignment = brains.chooseAssignment(game, awaitingDecision);

            if (assignment == null || assignment.isEmpty()) {
                return "";
            }

            return assignment.entrySet().stream()
                    .map(entry -> entry.getKey() + " " + String.join(" ", entry.getValue()))
                    .collect(Collectors.joining(","));
        }

        throw new UnsupportedOperationException("Decision not supported: " + awaitingDecision.toJson().toString());
    }

    private String makeCardSelectionDecision(DefaultLotroGame game, AwaitingDecision awaitingDecision) {
        if (DecisionClassifier.isDegenerateDecision(awaitingDecision)) {
            // No option to make a decision
            return super.chooseAction(game, awaitingDecision);
        }

        if (DecisionClassifier.isAssignArcheryShadowPlayerDecision(awaitingDecision)) {
            List<PhysicalCard> options = new ArrayList<>();
            List<String> cardIds = Arrays.stream(awaitingDecision.getDecisionParameters().get("cardId")).toList();
            for (String cardId : cardIds) {
                options.add(game.getGameState().getPhysicalCard(Integer.parseInt(cardId)));
            }
            PhysicalCard toWound = brains.chooseOwnMinionToWound(game, options);

            return String.valueOf(toWound.getCardId());
        }

        if (DecisionClassifier.isAssignArcheryFpPlayerDecision(awaitingDecision)) {
            List<PhysicalCard> options = new ArrayList<>();
            List<String> cardIds = Arrays.stream(awaitingDecision.getDecisionParameters().get("cardId")).toList();
            for (String cardId : cardIds) {
                options.add(game.getGameState().getPhysicalCard(Integer.parseInt(cardId)));
            }
            PhysicalCard toWound = brains.chooseOwnFpCharacterToWound(game, options);

            return String.valueOf(toWound.getCardId());
        }

        if (DecisionClassifier.isSanctuaryHealingDecision(awaitingDecision)) {
            List<PhysicalCard> options = new ArrayList<>();
            List<String> cardIds = Arrays.stream(awaitingDecision.getDecisionParameters().get("cardId")).toList();
            for (String cardId : cardIds) {
                options.add(game.getGameState().getPhysicalCard(Integer.parseInt(cardId)));
            }
            PhysicalCard toHeal = brains.chooseOwnFpCharacterToHeal(game, options);

            return String.valueOf(toHeal.getCardId());
        }

        if (DecisionClassifier.isOptionalReconcileDecision(awaitingDecision)) {
            List<PhysicalCard> toDiscard = brains.chooseCardsFromHandToDiscardToReconcile(game, 0);
            if (toDiscard.isEmpty()) {
                return ""; // pass
            }
            return String.valueOf(toDiscard.getFirst().getCardId());
        }

        if (DecisionClassifier.isMandatoryReconcileDiscardDecision(awaitingDecision)) {
            int min = Integer.parseInt(awaitingDecision.getDecisionParameters().get("min")[0]);
            List<PhysicalCard> toDiscard = brains.chooseCardsFromHandToDiscardToReconcile(game, min);
            return String.join(",", toDiscard.stream().map(card -> String.valueOf(card.getCardId())).toList());
        }

        if (DecisionClassifier.isChooseSkirmishOrderDecision(awaitingDecision)) {
            List<PhysicalCard> options = new ArrayList<>();
            List<String> cardIds = Arrays.stream(awaitingDecision.getDecisionParameters().get("cardId")).toList();
            for (String cardId : cardIds) {
                options.add(game.getGameState().getPhysicalCard(Integer.parseInt(cardId)));
            }
            PhysicalCard toChoose = brains.chooseNextSkirmishToResolve(game, options);

            return String.valueOf(toChoose.getCardId());
        }

        if (DecisionClassifier.isCardTargetingDecision(awaitingDecision)) {
            List<PhysicalCard> options = new ArrayList<>();
            int sourceId = Integer.parseInt(awaitingDecision.getDecisionParameters().get("source")[0]);
            PhysicalCard source = game.getGameState().getPhysicalCard(sourceId);
            List<String> cardIds = Arrays.stream(awaitingDecision.getDecisionParameters().get("cardId")).toList();
            for (String cardId : cardIds) {
                options.add(game.getGameState().getPhysicalCard(Integer.parseInt(cardId)));
            }

            return brains.chooseTargetForEffect(game, options, source, awaitingDecision);
        }

        throw new UnsupportedOperationException("Decision not supported: " + awaitingDecision.toJson().toString());
    }

    private String makeArbitraryCardsSelectionDecision(DefaultLotroGame game, AwaitingDecision awaitingDecision) {
        if (DecisionClassifier.isDegenerateDecision(awaitingDecision)) {
            // No option to make a decision
            return super.chooseAction(game, awaitingDecision);
        }

        if (DecisionClassifier.isStartingFellowshipDecision(awaitingDecision)) {
            String wantedOption = brains.chooseNextStartingCompanionToPlay(game);

            if (wantedOption.isEmpty()) {
                return ""; // Pass
            }

            List<String> cardIds = Arrays.stream(awaitingDecision.getDecisionParameters().get("cardId")).toList();
            List<String> blueprints = Arrays.stream(awaitingDecision.getDecisionParameters().get("blueprintId")).toList();

            for (int i = 0; i < blueprints.size(); i++) {
                if (blueprints.get(i).equals(wantedOption)) {
                    return cardIds.get(i);
                }
            }

            throw new UnsupportedOperationException("Decision not supported: " + awaitingDecision.toJson().toString());
        }

        if (DecisionClassifier.isArbitraryCardTargetingDecision(awaitingDecision)) {
            List<PhysicalCard> options = new ArrayList<>();
            int sourceId = Integer.parseInt(awaitingDecision.getDecisionParameters().get("source")[0]);
            PhysicalCard source = game.getGameState().getPhysicalCard(sourceId);
            List<String> cardIds = Arrays.stream(awaitingDecision.getDecisionParameters().get("physicalId")).toList();
            for (String cardId : cardIds) {
                options.add(game.getGameState().getPhysicalCard(Integer.parseInt(cardId)));
            }

            return brains.chooseTargetForEffect(game, options, source, awaitingDecision);
        }

        throw new UnsupportedOperationException("Decision not supported: " + awaitingDecision.toJson().toString());
    }

    private String makeMultipleChoiceDecision(LotroGame game, AwaitingDecision awaitingDecision) {
        if (DecisionClassifier.isGoFirstDecision(awaitingDecision)) {
            String wantedOption = brains.wantToGoFirst(game) ? "Go first" : "Go second";
            return getWantedOptionIndex(awaitingDecision, wantedOption);
        }

        if (DecisionClassifier.isMulliganDecision(awaitingDecision)) {
            String wantedOption = brains.wantToMulligan(game) ? "Yes" : "No";
            return getWantedOptionIndex(awaitingDecision, wantedOption);
        }

        if (DecisionClassifier.isMoveAgainDecision(awaitingDecision)) {
            String wantedOption = brains.wantToMoveAgain(game) ? "Yes" : "No";
            return getWantedOptionIndex(awaitingDecision, wantedOption);
        }

        if (DecisionClassifier.isCardMultipleChoiceDecision(awaitingDecision)) {
            try {
                String wantedOption = brains.chooseOptionForEffect(game, awaitingDecision);
                return getWantedOptionIndex(awaitingDecision, wantedOption);
            } catch (CardNotFoundException e) {
                throw new UnsupportedOperationException("Decision not supported: " + awaitingDecision.toJson().toString());
            }
        }

        throw new UnsupportedOperationException("Decision not supported: " + awaitingDecision.toJson().toString());
    }

    private static String getWantedOptionIndex(AwaitingDecision awaitingDecision, String wantedOption) {
        String[] options = awaitingDecision.getDecisionParameters().get("results");
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(wantedOption)) {
                return String.valueOf(i);
            }
        }
        throw new UnsupportedOperationException("Decision not supported: " + awaitingDecision.toJson().toString());
    }

    private String makeIntegerDecision(LotroGame game, AwaitingDecision awaitingDecision) {
        if (DecisionClassifier.isBurdenBidDecision(awaitingDecision)) {
            int burdensToBid = brains.chooseBurdensToBid(game);
            return String.valueOf(burdensToBid);
        }

        if (DecisionClassifier.isCardIntegerChoiceDecision(awaitingDecision)) {
            try {
                int wantedOption = brains.chooseIntegerForEffect(game, awaitingDecision);
                return String.valueOf(wantedOption);
            } catch (CardNotFoundException e) {
                throw new UnsupportedOperationException("Decision not supported: " + awaitingDecision.toJson().toString());
            }
        }

        throw new UnsupportedOperationException("Decision not supported: " + awaitingDecision.toJson().toString());
    }
}
