package com.gempukku.lotro.bots.rl.v2.decisions.choice;

import com.gempukku.lotro.bots.rl.learning.semanticaction.MultipleChoiceAction;
import com.gempukku.lotro.bots.rl.v2.ModelRegistryV2;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class GoFirstAnswerer extends AbstractChoiceAnswerer {
    @Override
    protected String getTextTrigger() {
        return null; // Not used
    }

    @Override
    protected List<String> getOptions() {
        return List.of("Go first", "Go second");
    }

    @Override
    public double[] extractFeatures(GameState gameState, AwaitingDecision decision, String playerName) {
        // Nothing know when choosing, will need different instances of model registry for different decks if training is to be used
        return new double[0];
    }

    @Override
    public boolean appliesTo(GameState gameState, AwaitingDecision decision, String playerName) {
        if (decision.getDecisionType() != AwaitingDecisionType.MULTIPLE_CHOICE)
            return false;

        String[] options = decision.getDecisionParameters().get("results");
        if (options.length != getOptions().size())
            return false;

        List<String> inputOptions = Arrays.stream(options).map(String::trim).collect(Collectors.toList());
        List<String> normalizedCardOptions = getOptions().stream().map(String::trim).collect(Collectors.toList());

        return new HashSet<>(inputOptions).equals(new HashSet<>(normalizedCardOptions));
    }

    @Override
    public String getAnswer(GameState gameState, AwaitingDecision decision, String playerName, ModelRegistryV2 modelRegistry) {
        // Always go first
        return new MultipleChoiceAction(getOptions().getFirst()).toDecisionString(decision, gameState);
    }
}
