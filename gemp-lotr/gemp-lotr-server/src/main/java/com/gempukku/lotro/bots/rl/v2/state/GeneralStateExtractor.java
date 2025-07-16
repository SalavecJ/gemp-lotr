package com.gempukku.lotro.bots.rl.v2.state;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

public class GeneralStateExtractor {

    public static double[] extractFeatures(GameState gameState, AwaitingDecision decision, String playerId) {
        String opponent = gameState.getPlayerNames().stream()
                .filter(s -> !s.equals(playerId)).findFirst()
                .orElseThrow(() -> new IllegalStateException("Unknown second player"));

        List<Double> features = new ArrayList<>();
        // FP / Shadow player
        features.add(getTurnIndicator(gameState, playerId));
        // Twilight
        features.add((double) gameState.getTwilightPool());
        // Positions on adventure path
        features.add((double) gameState.getPlayerPosition(playerId));
        features.add((double) gameState.getPlayerPosition(opponent));
        // Number of companions
        features.add((double) gameState.getInPlay().stream().filter((Predicate<PhysicalCard>) physicalCard -> physicalCard.getOwner().equals(playerId) && physicalCard.getBlueprint().getCardType().equals(CardType.COMPANION)).count());
        features.add((double) gameState.getInPlay().stream().filter((Predicate<PhysicalCard>) physicalCard -> physicalCard.getOwner().equals(opponent) && physicalCard.getBlueprint().getCardType().equals(CardType.COMPANION)).count());
        // Total companion strength
        features.add((double) gameState.getInPlay().stream().filter((Predicate<PhysicalCard>) physicalCard -> physicalCard.getOwner().equals(playerId) && physicalCard.getBlueprint().getCardType().equals(CardType.COMPANION)).mapToInt((ToIntFunction<PhysicalCard>) value -> value.getBlueprint().getStrength()).sum());
        features.add((double) gameState.getInPlay().stream().filter((Predicate<PhysicalCard>) physicalCard -> physicalCard.getOwner().equals(opponent) && physicalCard.getBlueprint().getCardType().equals(CardType.COMPANION)).mapToInt((ToIntFunction<PhysicalCard>) value -> value.getBlueprint().getStrength()).sum());
        // Number of minions
        features.add((double) gameState.getInPlay().stream().filter((Predicate<PhysicalCard>) physicalCard -> physicalCard.getOwner().equals(opponent) && physicalCard.getBlueprint().getCardType().equals(CardType.MINION)).count());
        // Total minion strength
        features.add((double) gameState.getInPlay().stream().filter((Predicate<PhysicalCard>) physicalCard -> physicalCard.getOwner().equals(opponent) && physicalCard.getBlueprint().getCardType().equals(CardType.MINION)).mapToInt((ToIntFunction<PhysicalCard>) value -> value.getBlueprint().getStrength()).sum());
        // Number of attached cards on companions
        features.add((double) gameState.getInPlay().stream().filter((Predicate<PhysicalCard>) physicalCard -> physicalCard.getOwner().equals(playerId) && physicalCard.getAttachedTo() != null && physicalCard.getAttachedTo().getBlueprint().getCardType().equals(CardType.COMPANION)).count());
        features.add((double) gameState.getInPlay().stream().filter((Predicate<PhysicalCard>) physicalCard -> physicalCard.getOwner().equals(opponent) && physicalCard.getAttachedTo() != null && physicalCard.getAttachedTo().getBlueprint().getCardType().equals(CardType.COMPANION)).count());
        // Number of attached cards on minions
        features.add((double) gameState.getInPlay().stream().filter((Predicate<PhysicalCard>) physicalCard -> physicalCard.getOwner().equals(opponent) && physicalCard.getAttachedTo() != null && physicalCard.getBlueprint().getCardType().equals(CardType.MINION)).count());
        // Hand situation
        features.add((double) gameState.getHand(playerId).stream().filter((Predicate<PhysicalCard>) physicalCard -> physicalCard.getBlueprint().getSide().equals(Side.FREE_PEOPLE)).count());
        features.add((double) gameState.getHand(playerId).stream().filter((Predicate<PhysicalCard>) physicalCard -> physicalCard.getBlueprint().getSide().equals(Side.FREE_PEOPLE) && physicalCard.getBlueprint().getCardType().equals(CardType.EVENT) && physicalCard.getBlueprint().hasTimeword(Timeword.SKIRMISH)).count());
        features.add((double) gameState.getHand(playerId).stream().filter((Predicate<PhysicalCard>) physicalCard -> physicalCard.getBlueprint().getSide().equals(Side.SHADOW)).count());
        features.add((double) gameState.getHand(opponent).size());
        // Cards in discard -> cards remaining in deck
        features.add((double) gameState.getDeck(playerId).size());
        features.add((double) gameState.getDiscard(opponent).size());
        // Burden count
        features.add((double) gameState.getPlayerBurdens(playerId));
        features.add((double) gameState.getPlayerBurdens(opponent));
        // Total wounds on companions
        features.add((double) gameState.getInPlay().stream().filter((Predicate<PhysicalCard>) physicalCard -> physicalCard.getOwner().equals(playerId)).mapToInt((ToIntFunction<PhysicalCard>) gameState::getWounds).sum());
        features.add((double) gameState.getInPlay().stream().filter((Predicate<PhysicalCard>) physicalCard -> physicalCard.getOwner().equals(opponent)).mapToInt((ToIntFunction<PhysicalCard>) gameState::getWounds).sum());

        return features.stream().mapToDouble(Double::doubleValue).toArray();
    }

    private static double getTurnIndicator(GameState gameState, String playerId) {
        String current = gameState.getCurrentPlayerId();
        return current == null ? -1.0 : (current.equals(playerId) ? 1.0 : 0.0);
    }
}
