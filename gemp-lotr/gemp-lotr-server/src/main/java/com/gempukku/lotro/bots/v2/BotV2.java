package com.gempukku.lotro.bots.v2;

import com.gempukku.lotro.bots.BotPlayer;
import com.gempukku.lotro.bots.v2.decision.Decider;
import com.gempukku.lotro.bots.v2.decision.option.Option;
import com.gempukku.lotro.bots.v2.decision.request.DecisionRequest;
import com.gempukku.lotro.bots.v2.decision.request.DecisionRequestFactory;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.List;

public class BotV2 implements BotPlayer {
    private final String botName;
    private final Decider decider;

    public BotV2(String botName, Decider decider) {
        this.botName = botName;
        this.decider = decider;
    }

    @Override
    public String chooseAction(DefaultLotroGame game, AwaitingDecision awaitingDecision) {
        try {
            DecisionRequest decisionRequest = DecisionRequestFactory.create(game, awaitingDecision);

            BotLogging.log(1, decisionRequest.getText(), true);
            for (int i = 0; i < decisionRequest.getOptions().size(); i++) {
                BotLogging.log(1, (i + 1) + ": " + decisionRequest.getOptions().get(i));
            }

            List<Option> chosenOptions = decider.decide(decisionRequest, game);
            if (chosenOptions.isEmpty()) {
                throw new IllegalStateException("No options chosen for decision: " + awaitingDecision.toJson().toString());
            } else if (chosenOptions.size() == 1) {
                BotLogging.log(1, "Chosen option: " + chosenOptions.getFirst());
                return chosenOptions.getFirst().getAnswerForGame();
            } else {
                BotLogging.log(1, "Chosen options: " + String.join("; ", chosenOptions.stream().map(Option::toString).toList()));
                return decisionRequest.combineSelected(chosenOptions);
            }
        } catch (Exception e) {
            BotLogging.log(1, e.getMessage(), true);
            throw new UnsupportedOperationException("Error while processing decision: " + awaitingDecision.toJson().toString(), e);
//            return new RandomDecisionBot(botName).chooseAction(game, awaitingDecision);
        }
    }

    @Override
    public void decisionMadeByPlayer(DefaultLotroGame game, AwaitingDecision awaitingDecision, String answer, String player) {

    }

    @Override
    public void cleanUpAfterGame() {

    }

    @Override
    public String getName() {
        return botName;
    }
}
