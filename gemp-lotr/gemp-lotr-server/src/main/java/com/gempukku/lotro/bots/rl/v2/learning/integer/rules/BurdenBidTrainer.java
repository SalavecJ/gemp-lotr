package com.gempukku.lotro.bots.rl.v2.learning.integer.rules;

import com.gempukku.lotro.bots.BotService;
import com.gempukku.lotro.bots.rl.v2.ModelRegistryV2;
import com.gempukku.lotro.bots.rl.v2.learning.integer.AbstractIntegerTrainer;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

import java.util.Random;

public class BurdenBidTrainer extends AbstractIntegerTrainer {

    @Override
    protected String getTextTrigger() {
        return "burdens to bid";
    }

    @Override
    public String getAnswer(GameState gameState, AwaitingDecision decision, String playerName, ModelRegistryV2 modelRegistry) {
        // Bid randomly less than half of resistance
        int resistance = 10;
        if (BotService.staticLibrary != null) {
            try {
                resistance = BotService.staticLibrary.getLotroCardBlueprint(gameState.getLotroDeck(playerName).getRingBearer()).getResistance();
            } catch (CardNotFoundException ignored) {

            }
        }
        Random random = new Random();
        int randomNumber = random.nextInt(resistance / 2);
        return String.valueOf(randomNumber);
    }
}
