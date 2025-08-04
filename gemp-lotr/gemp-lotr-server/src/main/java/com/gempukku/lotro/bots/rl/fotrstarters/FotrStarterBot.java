package com.gempukku.lotro.bots.rl.fotrstarters;

import com.gempukku.lotro.bots.random.RandomDecisionBot;
import com.gempukku.lotro.bots.rl.*;
import com.gempukku.lotro.bots.rl.ModelRegistry;
import com.gempukku.lotro.bots.rl.fotrstarters.models.arbitrarycards.CardFromDiscardTrainer;
import com.gempukku.lotro.bots.rl.fotrstarters.models.arbitrarycards.StartingFellowshipTrainer;
import com.gempukku.lotro.bots.rl.fotrstarters.models.assignment.FpAssignmentTrainer;
import com.gempukku.lotro.bots.rl.fotrstarters.models.assignment.ShadowAssignmentTrainer;
import com.gempukku.lotro.bots.rl.fotrstarters.models.cardaction.*;
import com.gempukku.lotro.bots.rl.fotrstarters.models.cardselection.*;
import com.gempukku.lotro.bots.rl.fotrstarters.models.integerchoice.BurdenTrainer;
import com.gempukku.lotro.bots.rl.fotrstarters.models.multiplechoice.AnotherMoveTrainer;
import com.gempukku.lotro.bots.rl.fotrstarters.models.multiplechoice.GoFirstTrainer;
import com.gempukku.lotro.bots.rl.fotrstarters.models.multiplechoice.MulliganTrainer;
import com.gempukku.lotro.bots.rl.learning.LearningBotPlayer;
import com.gempukku.lotro.bots.rl.learning.LearningStep;
import com.gempukku.lotro.bots.rl.learning.ReplayBuffer;
import com.gempukku.lotro.bots.rl.learning.semanticaction.*;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.state.Skirmish;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.CardActionSelectionDecision;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class FotrStarterBot extends RandomDecisionBot implements LearningBotPlayer {
    private final List<DecisionAnswerer> cardSelectionTrainers = List.of(
            new ReconcileTrainer(),
            new SanctuaryTrainer(),
            new ArcheryWoundTrainer(),
            new AttachItemTrainer(),
            new SkirmishOrderTrainer(),
            new HealTrainer(),
            new DiscardFromHandTrainer(),
            new ExertTrainer(),
            new DiscardFromPlayTrainer(),
            new PlayFromHandTrainer()
    );

    private final List<DecisionAnswerer> fallBackTrainers = List.of(
            new FallBackCardSelectionTrainer() // card selection
    );

    private final List<DecisionAnswerer> integerTrainers = List.of(
            new BurdenTrainer()
    );

    private final List<DecisionAnswerer> multipleChoiceTrainers = List.of(
            new GoFirstTrainer(),
            new MulliganTrainer(),
            new AnotherMoveTrainer()
    );

    private final List<DecisionAnswerer> arbitraryCardsTrainers = List.of(
            new CardFromDiscardTrainer(),
            new StartingFellowshipTrainer()
    );

    private final List<DecisionAnswerer> cardActionTrainers = List.of(
            new FellowshipCardActionAnswerer(),
            new ShadowCardActionAnswerer(),
            new ManeuverCardActionAnswerer(),
            new SkirmishFpCardActionAnswerer(),
            new SkirmishShadowCardActionAnswerer(),
            new RegroupCardActionAnswerer(),
            new OptionalResponsesCardActionTrainer()
    );

    private final List<DecisionAnswerer> assignmentTrainers = List.of(
            new ShadowAssignmentTrainer(),
            new FpAssignmentTrainer()
    );

    private final RLGameStateFeatures features;
    private final ModelRegistry modelRegistry;
    private final ReplayBuffer replayBuffer;

    private CardActionSelectionDecision lastDecision = null;
    private int decisionRepeat = 0;
    private String lastAction = null;
    private int answerRepeat = 0;

    private final List<LearningStep> episodeSteps = new ArrayList<>();

    public FotrStarterBot(RLGameStateFeatures features, String playerId, ModelRegistry modelRegistry, ReplayBuffer replayBuffer) {
        super(playerId);
        this.features = features;
        this.modelRegistry = modelRegistry;
        this.replayBuffer = replayBuffer;
    }

    @Override
    public String chooseAction(LotroGame game, AwaitingDecision decision) {
        double[] stateVector = features.extractFeatures(game.getGameState(), decision, getName());

        SemanticAction action = chooseSemanticAction(game, decision);

        // Store temporarily — reward comes later
        episodeSteps.add(new LearningStep(stateVector, action, getName().equals(game.getGameState().getCurrentPlayerId()), decision));

        return action.toDecisionString(decision, game.getGameState());
    }

    private SemanticAction chooseSemanticAction(LotroGame game, AwaitingDecision decision) {
        String action =  switch (decision.getDecisionType()) {
            case INTEGER -> chooseIntegerAction(game, decision);
            case MULTIPLE_CHOICE -> chooseMultipleChoiceAction(game, decision);
            case ARBITRARY_CARDS -> chooseArbitraryCardsAction(game, decision);
            case CARD_ACTION_CHOICE -> chooseCardActionChoice(game, decision);
            case ACTION_CHOICE -> chooseActionChoice(game, decision);
            case CARD_SELECTION -> chooseCardSelectionAction(game, decision);
            case ASSIGN_MINIONS -> chooseAssignmentAction(game, decision);
        };

        return switch (decision.getDecisionType()) {
            case INTEGER -> new IntegerChoiceAction(Integer.parseInt(action));
            case MULTIPLE_CHOICE -> new MultipleChoiceAction(action, decision);
            case ARBITRARY_CARDS -> new ChooseFromArbitraryCardsAction(action, decision);
            case CARD_ACTION_CHOICE -> new CardActionChoiceAction(action, decision, game.getGameState());
            case ACTION_CHOICE -> new ActionChoiceAction(action, decision);
            case CARD_SELECTION -> {
                if (game.getGameState().getCurrentPhase().equals(Phase.SKIRMISH)) {
                    yield new CardSelectionAssignedAction(action, decision, game.getGameState());
                }
                yield new CardSelectionAction(action, decision, game.getGameState());
            }
            case ASSIGN_MINIONS -> new AssignMinionsAction(action, decision, game.getGameState(), game.getGameState().getCurrentPlayerId().equals(getName()));
        };
    }

    private String chooseAssignmentAction(LotroGame game, AwaitingDecision decision) {
        for (DecisionAnswerer trainer : assignmentTrainers) {
            if (trainer.appliesTo(game.getGameState(), decision, getName())) {
                return trainer.getAnswer(game.getGameState(), decision, getName(), features, modelRegistry);
            }
        }

        System.out.println("Unknown assign minions decision: "
                + game.getGameState().getCurrentPhase() + " - "
                + decision.getText()
                + "; freeCharacters=" + Arrays.toString(decision.getDecisionParameters().get("freeCharacters"))
                + "; minions=" + Arrays.toString(decision.getDecisionParameters().get("minions")));
        return super.chooseAction(game, decision);
    }

    private String chooseActionChoice(LotroGame game, AwaitingDecision decision) {
        // Order does not matter with FotR starters
//        System.out.println("Unknown action decision: "
//                + gameState.getCurrentPhase() + " - "
//                + decision.getText()
//                + "; actionId=" + Arrays.toString(decision.getDecisionParameters().get("actionId"))
//                + "; blueprintId=" + Arrays.toString(decision.getDecisionParameters().get("blueprintId"))
//                + "; actionText=" + Arrays.toString(decision.getDecisionParameters().get("actionText")));
        return super.chooseAction(game, decision);
    }

    private String chooseCardActionChoice(LotroGame game, AwaitingDecision decision) {
        String[] actionIds = decision.getDecisionParameters().get("actionId");
        if (actionIds == null || actionIds.length == 0) {
            // No actions available: must pass
            return "";
        }

        String chosenAnswer = null;
        for (DecisionAnswerer trainer : cardActionTrainers) {
            if (trainer.appliesTo(game.getGameState(), decision, getName())) {
                chosenAnswer = trainer.getAnswer(game.getGameState(), decision, getName(), features, modelRegistry);
            }
        }
        // Loop prevention
        if (chosenAnswer != null) {
            if (decision.equals(lastDecision) && chosenAnswer.equals(lastAction)) {
                answerRepeat++;
                decisionRepeat++;
                if (answerRepeat >= 3) {
                    answerRepeat = 0;
                    lastAction = null;
                    return "";
                }
            } else if (decision.equals(lastDecision)) {
                decisionRepeat++;
                if (decisionRepeat >= 6) {
                    answerRepeat = 0;
                    lastAction = null;
                    return "";
                }
            } else {
                lastDecision = (CardActionSelectionDecision) decision;
                decisionRepeat = 0;
                answerRepeat = 0;
                lastAction = chosenAnswer;
            }
            return chosenAnswer;
        }

        System.out.println("Unknown card action decision: "
                + game.getGameState().getCurrentPhase() + " - "
                + decision.getText()
                + "; actionId=" + Arrays.toString(decision.getDecisionParameters().get("actionId"))
                + "; cardId=" + Arrays.toString(decision.getDecisionParameters().get("cardId"))
                + "; blueprintId=" + Arrays.toString(decision.getDecisionParameters().get("blueprintId"))
                + "; actionText=" + Arrays.toString(decision.getDecisionParameters().get("actionText")));
        return super.chooseAction(game, decision);
    }

    private String chooseArbitraryCardsAction(LotroGame game, AwaitingDecision decision) {
        Map<String, String[]> params = decision.getDecisionParameters();
        String[] cardIds = params.get("cardId");
        String[] selectable = params.get("selectable");
        int min = params.containsKey("min") ? Integer.parseInt(params.get("min")[0]) : 0;
        int max = params.containsKey("max") ? Integer.parseInt(params.get("max")[0]) : cardIds.length;

        // Check if something is selectable
        if (cardIds == null || selectable == null || cardIds.length != selectable.length)
            return ""; // Invalid input
        // Collect all selectable indices
        List<Integer> selectableIndices = new ArrayList<>();
        for (int i = 0; i < selectable.length; i++) {
            if (Boolean.parseBoolean(selectable[i])) {
                selectableIndices.add(i);
            }
        }
        // If nothing is selectable or max == 0, just return empty
        if (selectableIndices.isEmpty() || max == 0)
            return "";

        for (DecisionAnswerer trainer : arbitraryCardsTrainers) {
            if (trainer.appliesTo(game.getGameState(), decision, getName())) {
                return trainer.getAnswer(game.getGameState(), decision, getName(), features, modelRegistry);
            }
        }

        // Fallback to random decision
        System.out.println("Unknown arbitrary card selection action: "
                + game.getGameState().getCurrentPhase() + " - "
                + decision.getText()
                + " (" + decision.getDecisionParameters().get("min")[0]
                + ";" + decision.getDecisionParameters().get("max")[0] + ") "
                + "cards=" + Arrays.toString(decision.getDecisionParameters().get("blueprintId"))
                + ", selectable=" + Arrays.toString(decision.getDecisionParameters().get("selectable")));
        return super.chooseAction(game, decision);
    }

    private String chooseCardSelectionAction(LotroGame game, AwaitingDecision decision) {
        int min = Integer.parseInt(decision.getDecisionParameters().get("min")[0]);
        int max = Integer.parseInt(decision.getDecisionParameters().get("max")[0]);
        List<String> cardIds = Arrays.stream(decision.getDecisionParameters().get("cardId")).toList();

        if (min == max && min == cardIds.size()) {
            // Need to choose all
            return String.join(",", cardIds);
        }

        // Choose the one skirmishing
        if (game.getGameState().getSkirmish() != null &&
                game.getGameState().getSkirmish().getFellowshipCharacter() != null &&
                game.getGameState().getSkirmish().getShadowCharacters() != null &&
                !game.getGameState().getSkirmish().getShadowCharacters().isEmpty()
                && min == 1 && max == 1) {
            Skirmish skirmish = game.getGameState().getSkirmish();
            for (String cardId : cardIds) {
                if (skirmish.getFellowshipCharacter().getCardId() == Integer.parseInt(cardId)) {
                    return cardId;
                }
                for (PhysicalCard shadowCharacter : skirmish.getShadowCharacters()) {
                    if (shadowCharacter.getCardId() == Integer.parseInt(cardId)) {
                        return cardId;
                    }
                }
            }
            // Skirmishing character cannot be chosen, choose whatever
            return super.chooseAction(game, decision);
        }

        for (DecisionAnswerer trainer : cardSelectionTrainers) {
            if (trainer.appliesTo(game.getGameState(), decision, getName())) {
                return trainer.getAnswer(game.getGameState(), decision, getName(), features, modelRegistry);
            }
        }

        // Last fallback to trainer
        for (DecisionAnswerer trainer : fallBackTrainers) {
            if (trainer.appliesTo(game.getGameState(), decision, getName())) {
                return trainer.getAnswer(game.getGameState(), decision, getName(), features, modelRegistry);
            }
        }

        // Fallback to random decision
        System.out.println("Unknown card selection action: " + game.getGameState().getCurrentPhase() + " - " + decision.getText() + " (" + decision.getDecisionParameters().get("min")[0] + ";" + decision.getDecisionParameters().get("max")[0] + ") " + Arrays.toString(decision.getDecisionParameters().get("cardId")));
        return super.chooseAction(game, decision);
    }

    private String chooseIntegerAction(LotroGame game, AwaitingDecision decision) {
        for (DecisionAnswerer trainer : integerTrainers) {
            if (trainer.appliesTo(game.getGameState(), decision, getName())) {
                return trainer.getAnswer(game.getGameState(), decision, getName(), features, modelRegistry);
            }
        }
        if (decision.getText().contains("how many to spot")) {
            return decision.getDecisionParameters().get("max")[0];
        }

        System.out.println("Unknown integer action: " + decision.getText());
        return super.chooseAction(game, decision);
    }

    private String chooseMultipleChoiceAction(LotroGame game, AwaitingDecision decision) {
        String[] options = decision.getDecisionParameters().get("results");
        for (DecisionAnswerer trainer : multipleChoiceTrainers) {
            if (trainer.appliesTo(game.getGameState(), decision, getName())) {
                return trainer.getAnswer(game.getGameState(), decision, getName(), features, modelRegistry);
            }
        }
        if (List.of(options).contains("Heal a companion") && List.of(options).contains("Remove a Shadow condition from a companion")) {
            return new MultipleChoiceAction("Heal a companion").toDecisionString(decision, game.getGameState());
        }
        if (List.of(options).contains("Exert Frodo") && List.of(options).contains("Exert 2 other companions")) {
            return new MultipleChoiceAction("Exert Frodo").toDecisionString(decision, game.getGameState());
        }

        System.out.println("Unknown multiple choice action: " + decision.getText() + " - " + Arrays.toString(options));
        return super.chooseAction(game, decision);
    }

    @Override
    public void observe(GameState gameState, AwaitingDecision decision, String playerId, String chosenAction, double reward, boolean terminal) {
        // At this stage, we assume `chosenAction` was already used to resolve game state,
        // and we just want to record the final outcome of this decision if needed.
        // Right now, we store full episode and use reward only in `endEpisode`.
    }

    @Override
    public void endEpisode(double reward) {
        episodeSteps.forEach(learningStep -> learningStep.reward = reward);
        if (replayBuffer != null) {
            replayBuffer.addEpisode(new ArrayList<>(episodeSteps));
        }
        episodeSteps.clear();
    }
}
