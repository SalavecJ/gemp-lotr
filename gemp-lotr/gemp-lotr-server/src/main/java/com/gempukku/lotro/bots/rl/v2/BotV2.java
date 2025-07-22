package com.gempukku.lotro.bots.rl.v2;

import com.gempukku.lotro.bots.random.RandomDecisionBot;
import com.gempukku.lotro.bots.rl.learning.*;
import com.gempukku.lotro.bots.rl.learning.semanticaction.*;
import com.gempukku.lotro.bots.rl.v2.decisions.AnswerersV2;
import com.gempukku.lotro.bots.rl.v2.decisions.DecisionAnswererV2;
import com.gempukku.lotro.bots.rl.v2.decisions.arbitrary.specific.SpecificArbitraryCardSelectionFactory;
import com.gempukku.lotro.bots.rl.v2.decisions.cardselection.specific.SpecificCardSelectionFactory;
import com.gempukku.lotro.bots.rl.v2.decisions.choice.specific.SpecificChoiceFactory;
import com.gempukku.lotro.bots.rl.v2.learning.SavedVector;
import com.gempukku.lotro.bots.rl.v2.learning.SavedVectorBuffer;
import com.gempukku.lotro.bots.rl.v2.learning.TrainerV2;
import com.gempukku.lotro.bots.rl.v2.learning.TrainersV2;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;

import java.util.ArrayList;
import java.util.List;

public class BotV2  extends RandomDecisionBot implements LearningBotPlayer {

    private final List<SavedVector> vectors = new ArrayList<>();
    private final SavedVectorBuffer replayBuffer;
    private final ModelRegistryV2 modelRegistry;

    public BotV2(String botName, SavedVectorBuffer replayBuffer, ModelRegistryV2 modelRegistry) {
        super(botName);
        this.replayBuffer = replayBuffer;
        this.modelRegistry = modelRegistry;
    }

    @Override
    public String chooseAction(GameState gameState, AwaitingDecision awaitingDecision) {
        SemanticAction action = chooseSemanticAction(gameState, awaitingDecision);

        // Store temporarily — reward comes later
        LearningStep dummyStep = new LearningStep(null, action, getName().equals(gameState.getCurrentPlayerId()), awaitingDecision);
        List<String> relevantTrainers = new ArrayList<>();
        for (TrainerV2 trainer : TrainersV2.getAllV2Trainers()) {
            if (trainer.isStepRelevant(dummyStep)) {
                vectors.addAll(trainer.toStringVectors(gameState, action, getName(), awaitingDecision));
                relevantTrainers.add(trainer.getName());
            }
        }
        if (relevantTrainers.size() > 1) {
            throw new IllegalStateException("Multiple trainers found relevant the same step - " + relevantTrainers + " - " + awaitingDecision.getText());
        }
        for (TrainerV2 trainer : TrainersV2.getAllV2GeneralTrainers()) {
            if (trainer.isStepRelevant(dummyStep)) {
                vectors.addAll(trainer.toStringVectors(gameState, action, getName(), awaitingDecision));
            }
        }

        return action.toDecisionString(awaitingDecision, gameState);
    }

    private SemanticAction chooseSemanticAction(GameState gameState, AwaitingDecision decision) {
        String action;

        try {
            action = switch (decision.getDecisionType()) {
                // TODO support actions with answerers
                case INTEGER -> chooseActionBasedOnModel(gameState, decision);
                case MULTIPLE_CHOICE -> chooseActionBasedOnModel(gameState, decision);
                case ARBITRARY_CARDS -> chooseActionBasedOnModel(gameState, decision);
//                case CARD_ACTION_CHOICE -> chooseCardActionChoice(gameState, decision);
                case ACTION_CHOICE -> chooseActionBasedOnModel(gameState, decision);
                case CARD_SELECTION -> chooseActionBasedOnModel(gameState, decision);
//            case ASSIGN_MINIONS -> chooseAssignmentAction(gameState, decision);
                default -> super.chooseAction(gameState, decision);
            };
        } catch (UnsupportedOperationException e) {
            // Cannot use models, choose at random and log it
            if (modelRegistry != null) {
                System.out.println(e.getMessage());
            }
            action = super.chooseAction(gameState, decision);
        }

        return switch (decision.getDecisionType()) {
            case INTEGER -> new IntegerChoiceAction(Integer.parseInt(action));
            case MULTIPLE_CHOICE -> new MultipleChoiceAction(action, decision);
            case ARBITRARY_CARDS -> new ChooseFromArbitraryCardsAction(action, decision);
            case CARD_ACTION_CHOICE -> new CardActionChoiceAction(action, decision, gameState);
            case ACTION_CHOICE -> new ActionChoiceAction(action, decision);
            case CARD_SELECTION -> {
                if (gameState.getCurrentPhase().equals(Phase.SKIRMISH)) {
                    yield new CardSelectionAssignedAction(action, decision, gameState);
                }
                yield new CardSelectionAction(action, decision, gameState);
            }
            case ASSIGN_MINIONS -> new AssignMinionsAction(action, decision, gameState, gameState.getCurrentPlayerId().equals(getName()));
        };
    }

    private String chooseActionBasedOnModel(GameState gameState, AwaitingDecision decision) {
        boolean modelError = false;
        for (DecisionAnswererV2 answerer : AnswerersV2.getAllV2Answerers()) {
            if (answerer.appliesTo(gameState, decision, getName())) {
                try {
                    return answerer.getAnswer(gameState, decision, getName(), modelRegistry);
                } catch (UnsupportedOperationException ignored) {
                    // Model not found for answerer
                    modelError = true;
                }
            }
        }

        if (!modelError) {
            // No answerer found, make one
            switch (decision.getDecisionType()) {
                case MULTIPLE_CHOICE -> SpecificChoiceFactory.makeAndRegisterTrainerAndAnswerer(decision);
                case ARBITRARY_CARDS ->
                        SpecificArbitraryCardSelectionFactory.makeAndRegisterTrainerAndAnswerer(decision);
                case CARD_SELECTION -> SpecificCardSelectionFactory.makeAndRegisterTrainerAndAnswerer(decision);
            }
        }

        // Try general answerers
        for (DecisionAnswererV2 answerer : AnswerersV2.getAllV2GeneralAnswerers()) {
            if (answerer.appliesTo(gameState, decision, getName())) {
                return answerer.getAnswer(gameState, decision, getName(), modelRegistry);
            }
        }

        if (decision.getDecisionType().equals(AwaitingDecisionType.ACTION_CHOICE)) {
            // Multiple actions triggering at the same time, choose whatever
            return super.chooseAction(gameState, decision);

        }

        throw new UnsupportedOperationException("Unsupported decision: " + decision.toJson().toString());
    }

    @Override
    public void observe(GameState gameState, AwaitingDecision decision, String playerId, String chosenAction, double reward, boolean terminal) {
        // At this stage, we assume `chosenAction` was already used to resolve game state,
        // and we just want to record the final outcome of this decision if needed.
        // Right now, we store full episode and use reward only in `endEpisode`.
    }

    @Override
    public void endEpisode(double reward) {
        if (replayBuffer != null) {
            vectors.forEach(vector -> vector.reward = reward);
            replayBuffer.addEpisode(new ArrayList<>(vectors));
        }
        vectors.clear();

    }
}
