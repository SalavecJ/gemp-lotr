package com.gempukku.lotro.bots.random;

import com.gempukku.lotro.bots.rl.learning.LearningBotPlayer;
import com.gempukku.lotro.bots.rl.learning.LearningStep;
import com.gempukku.lotro.bots.rl.RLGameStateFeatures;
import com.gempukku.lotro.bots.rl.learning.ReplayBuffer;
import com.gempukku.lotro.bots.rl.learning.semanticaction.*;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

import java.util.ArrayList;
import java.util.List;

public class RandomLearningBot extends RandomDecisionBot implements LearningBotPlayer {
    private final RLGameStateFeatures features;
    private final List<LearningStep> episodeSteps = new ArrayList<>();
    private final ReplayBuffer replayBuffer;


    public RandomLearningBot(RLGameStateFeatures features, String playerId, ReplayBuffer replayBuffer) {
        super(playerId);
        this.features = features;
        this.replayBuffer = replayBuffer;
    }

    @Override
    public String chooseAction(LotroGame game, AwaitingDecision decision) {
        double[] stateVector = features.extractFeatures(game.getGameState(), decision, getName());

        SemanticAction action = pickSemanticAction(decision, game);

        // Store temporarily — reward comes later
        episodeSteps.add(new LearningStep(stateVector, action, getName().equals(game.getGameState().getCurrentPlayerId()), decision));

        return action.toDecisionString(decision, game.getGameState());
    }

    private SemanticAction pickSemanticAction(AwaitingDecision decision, LotroGame game) {
        String action = super.chooseAction(game, decision);

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
