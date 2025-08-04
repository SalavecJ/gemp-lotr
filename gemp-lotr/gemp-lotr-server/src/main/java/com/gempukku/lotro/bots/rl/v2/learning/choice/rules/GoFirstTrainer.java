package com.gempukku.lotro.bots.rl.v2.learning.choice.rules;

import com.gempukku.lotro.bots.rl.v2.ModelRegistryV2;
import com.gempukku.lotro.bots.rl.v2.learning.choice.AbstractChoiceTrainer;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

public class GoFirstTrainer extends AbstractChoiceTrainer {
    @Override
    protected String getTextTrigger() {
        return null; // Not used
    }

    @Override
    protected int getNumberOfOptions() {
        return 2;
    }

    @Override
    public double[] extractFeatures(GameState gameState, AwaitingDecision decision, String playerName) {
        // Nothing known when choosing, will need different instances of model registry for different decks if training is to be used
        return new double[0];
    }

    @Override
    public boolean appliesTo(GameState gameState, AwaitingDecision decision, String playerName) {
        if (decision.getDecisionType() != AwaitingDecisionType.MULTIPLE_CHOICE)
            return false;

        String[] options = decision.getDecisionParameters().get("results");
        if (options.length != getNumberOfOptions())
            return false;

        List<String> inputOptions = Arrays.stream(options).map(String::trim).toList();
        List<String> normalizedCardOptions = Stream.of("Go first", "Go second").map(String::trim).toList();

        return new HashSet<>(inputOptions).equals(new HashSet<>(normalizedCardOptions));
    }

    @Override
    public String getAnswer(LotroGame game, AwaitingDecision decision, String playerName, ModelRegistryV2 modelRegistry) {
        // Always go first
        return String.valueOf(0);
    }
}
