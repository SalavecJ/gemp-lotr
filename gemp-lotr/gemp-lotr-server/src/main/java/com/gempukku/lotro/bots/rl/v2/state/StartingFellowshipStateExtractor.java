package com.gempukku.lotro.bots.rl.v2.state;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

import java.util.ArrayList;
import java.util.List;

public class StartingFellowshipStateExtractor {
    public static double[] extractFeatures(GameState gameState, AwaitingDecision decision, String playerId) {
        String opponent = gameState.getPlayerNames().stream()
                .filter(s -> !s.equals(playerId)).findFirst()
                .orElseThrow(() -> new IllegalStateException("Unknown second player"));
        List<Double> features = new ArrayList<>();

        int playersCompanionsInPlay = 0;
        for (PhysicalCard physicalCard : gameState.getInPlay()) {
            if (physicalCard.getOwner().equals(playerId) && physicalCard.getBlueprint().getCardType().equals(CardType.COMPANION)) {
                playersCompanionsInPlay++;
            }
        }


        // Starting player
        features.add(gameState.getFirstPlayerId().equals(playerId) ? 1.0 : 0.0);
        // Burden count
        features.add((double) gameState.getPlayerBurdens(playerId));
        features.add((double) gameState.getPlayerBurdens(opponent));
        // Twilight
        features.add((double) gameState.getTwilightPool());
        // Companions played already
        features.add((double) playersCompanionsInPlay);


        return features.stream().mapToDouble(Double::doubleValue).toArray();
    }
}
