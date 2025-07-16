package com.gempukku.lotro.bots.rl.v2.state;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class MulliganStateExtractor {
    public static double[] extractFeatures(GameState gameState, AwaitingDecision decision, String playerId) {
        List<Double> features = new ArrayList<>();
        // My turn, first player
        features.add(getTurnIndicator(gameState, playerId));
        // Number of fp and shadow cards in hand
        features.add((double) gameState.getHand(playerId).stream().filter((Predicate<PhysicalCard>) physicalCard -> physicalCard.getBlueprint().getSide().equals(Side.FREE_PEOPLE)).count());
        features.add((double) gameState.getHand(playerId).stream().filter((Predicate<PhysicalCard>) physicalCard -> physicalCard.getBlueprint().getSide().equals(Side.SHADOW)).count());
        // Number of companions and minions
        features.add((double) gameState.getHand(playerId).stream().filter((Predicate<PhysicalCard>) physicalCard -> physicalCard.getBlueprint().getSide().equals(Side.FREE_PEOPLE) && physicalCard.getBlueprint().getCardType().equals(CardType.COMPANION)).count());
        features.add((double) gameState.getHand(playerId).stream().filter((Predicate<PhysicalCard>) physicalCard -> physicalCard.getBlueprint().getSide().equals(Side.SHADOW) && physicalCard.getBlueprint().getCardType().equals(CardType.MINION)).count());

        return features.stream().mapToDouble(Double::doubleValue).toArray();
    }

    private static double getTurnIndicator(GameState gameState, String playerId) {
        String current = gameState.getCurrentPlayerId();
        return current == null ? -1.0 : (current.equals(playerId) ? 1.0 : 0.0);
    }
}
