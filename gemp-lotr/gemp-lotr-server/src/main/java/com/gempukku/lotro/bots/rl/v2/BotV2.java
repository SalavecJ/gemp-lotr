package com.gempukku.lotro.bots.rl.v2;

import com.gempukku.lotro.bots.random.RandomDecisionBot;
import com.gempukku.lotro.bots.rl.learning.*;
import com.gempukku.lotro.bots.rl.v2.learning.SavedVector;
import com.gempukku.lotro.bots.rl.v2.learning.SavedVectorBuffer;
import com.gempukku.lotro.bots.rl.v2.learning.TrainerV2;
import com.gempukku.lotro.bots.rl.v2.learning.TrainersV2;
import com.gempukku.lotro.bots.rl.v2.learning.arbitrary.specific.SpecificArbitraryCardSelectionTrainerFactory;
import com.gempukku.lotro.bots.rl.v2.learning.cardselection.specific.SpecificCardSelectionTrainerFactory;
import com.gempukku.lotro.bots.rl.v2.learning.choice.specific.SpecificChoiceTrainerFactory;
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
        String action = makeDecision(gameState, awaitingDecision);

        // Store temporarily — reward comes later
        List<String> relevantTrainers = new ArrayList<>();
        for (TrainerV2 trainer : TrainersV2.getAllV2Trainers()) {
            if (trainer.appliesTo(gameState, awaitingDecision, getName())) {
                vectors.addAll(trainer.toStringVectors(gameState, awaitingDecision, getName(), action));
                relevantTrainers.add(trainer.getName());
            }
        }
        if (relevantTrainers.size() > 1) {
            throw new IllegalStateException("Multiple trainers found relevant the same step - " + relevantTrainers + " - " + awaitingDecision.getText());
        }
        for (TrainerV2 trainer : TrainersV2.getAllV2GeneralTrainers()) {
            if (trainer.appliesTo(gameState, awaitingDecision, getName())) {
                vectors.addAll(trainer.toStringVectors(gameState, awaitingDecision, getName(), action));
            }
        }

        return action;
    }

    private String makeDecision(GameState gameState, AwaitingDecision decision) {
        try {
            return switch (decision.getDecisionType()) {
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
            return super.chooseAction(gameState, decision);
        }
    }

    private String chooseActionBasedOnModel(GameState gameState, AwaitingDecision decision) {
        // Try degenerate trainers first
        for (TrainerV2 trainer : TrainersV2.getAllV2DegenerateTrainers()) {
            if (trainer.appliesTo(gameState, decision, getName())) {
                return trainer.getAnswer(gameState, decision, getName(), modelRegistry);
            }
        }

        boolean modelError = false;
        for (TrainerV2 trainer : TrainersV2.getAllV2Trainers()) {
            if (trainer.appliesTo(gameState, decision, getName())) {
                try {
                    return trainer.getAnswer(gameState, decision, getName(), modelRegistry);
                } catch (UnsupportedOperationException ignored) {
                    // Model not found for answerer
                    modelError = true;
                }
            }
        }

        if (!modelError) {
            // No trainer found, make one
            switch (decision.getDecisionType()) {
                case MULTIPLE_CHOICE -> SpecificChoiceTrainerFactory.makeAndRegisterTrainer(decision);
                case ARBITRARY_CARDS ->
                        SpecificArbitraryCardSelectionTrainerFactory.makeAndRegisterTrainer(decision);
                case CARD_SELECTION -> SpecificCardSelectionTrainerFactory.makeAndRegisterTrainer(decision);
            }
        }

        // Try general trainers
        for (TrainerV2 trainer : TrainersV2.getAllV2GeneralTrainers()) {
            if (trainer.appliesTo(gameState, decision, getName())) {
                return trainer.getAnswer(gameState, decision, getName(), modelRegistry);
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
