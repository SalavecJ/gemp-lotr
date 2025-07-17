package com.gempukku.lotro.bots.rl.v2.state;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.Assignment;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

public class SkirmishOrderStateExtractor {
    public static double[] extractFeatures(GameState gameState, AwaitingDecision decision, String playerId) {
        String opponent = gameState.getPlayerNames().stream()
                .filter(s -> !s.equals(playerId)).findFirst()
                .orElseThrow(() -> new IllegalStateException("Unknown second player"));

        List<Double> features = new ArrayList<>();
        // Twilight
        features.add((double) gameState.getTwilightPool());
        // Number of companions
        features.add((double) gameState.getInPlay().stream().filter((Predicate<PhysicalCard>) physicalCard -> physicalCard.getOwner().equals(playerId) && physicalCard.getBlueprint().getCardType().equals(CardType.COMPANION)).count());
        // Number of minions
        features.add((double) gameState.getInPlay().stream().filter((Predicate<PhysicalCard>) physicalCard -> physicalCard.getOwner().equals(opponent) && physicalCard.getBlueprint().getCardType().equals(CardType.MINION)).count());
        // Total minion strength
        features.add((double) gameState.getInPlay().stream().filter((Predicate<PhysicalCard>) physicalCard -> physicalCard.getOwner().equals(opponent) && physicalCard.getBlueprint().getCardType().equals(CardType.MINION)).mapToInt((ToIntFunction<PhysicalCard>) value -> value.getBlueprint().getStrength()).sum());
        // Hand situation
        features.add((double) gameState.getHand(playerId).stream().filter((Predicate<PhysicalCard>) physicalCard -> physicalCard.getBlueprint().getSide().equals(Side.FREE_PEOPLE) && physicalCard.getBlueprint().getCardType().equals(CardType.EVENT) && physicalCard.getBlueprint().hasTimeword(Timeword.SKIRMISH)).count());
        features.add((double) gameState.getHand(opponent).size());
        // Burden count
        features.add((double) gameState.getPlayerBurdens(playerId));

        // Number of remaining skirmished
        features.add((double) gameState.getAssignments().size());

        // Ring bearer assigned
        boolean ringBearerAssigned = false;
        for (Assignment assignment : gameState.getAssignments()) {
            if (assignment.getFellowshipCharacter().getCardId() == gameState.getRingBearer(playerId).getCardId()) {
                ringBearerAssigned = true;
                break;
            }
        }
        features.add(ringBearerAssigned ? 1.0 : 0.0);

        return features.stream().mapToDouble(Double::doubleValue).toArray();
    }

    private static double getTurnIndicator(GameState gameState, String playerId) {
        String current = gameState.getCurrentPlayerId();
        return current == null ? -1.0 : (current.equals(playerId) ? 1.0 : 0.0);
    }
}
