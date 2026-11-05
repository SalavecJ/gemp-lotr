package com.gempukku.lotro.bots.v2.fotr_starter.utility;

import com.gempukku.lotro.bots.v2.fotr_starter.DeckRecognizer;
import com.gempukku.lotro.bots.v2.fotr_starter.FotrStarterDeck;
import com.gempukku.lotro.bots.v2.simulation.SimulatedGame;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.game.PhysicalCard;

import java.util.function.Predicate;

import static com.gempukku.lotro.bots.v2.fotr_starter.utility.CardPredicates.hasCardType;
import static com.gempukku.lotro.bots.v2.fotr_starter.utility.CardPredicates.hasFullName;

public final class GameQueries {
    private GameQueries() {
    }

    public static FotrStarterDeck getOpponentDeck(SimulatedGame simulatedGame, String player) {
        return DeckRecognizer.recognize(simulatedGame.getGame().getGameState().getLotroDeck(simulatedGame.getGame().getGameState().getOpponent(player)));
    }

    public static int getVitalitySaveThreshold(SimulatedGame simulatedGame, String player) {
        // need more than 3 vitality to exert against Uruks, more than 2 against Moria
        return getOpponentDeck(simulatedGame, player) == FotrStarterDeck.ARAGORN_STARTER ? 3 : 2;
    }

    public static boolean isInPlay(SimulatedGame simulatedGame, String fullName, String player) {
        return simulatedGame.getGame().getGameState().getInPlay().stream()
                .filter(card -> card.getOwner().equals(player))
                .anyMatch(hasFullName(fullName));
    }

    public static int getVitality(SimulatedGame simulatedGame, String fullName, String player) {
        PhysicalCard character = simulatedGame.getGame().getGameState().getInPlay().stream()
                .filter(hasFullName(fullName))
                .filter(card -> card.getOwner().equals(player))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(fullName + " not in play"));
        return simulatedGame.getGame().getModifiersQuerying().getVitality(simulatedGame.getGame(), character);
    }

    public static boolean hasWoundedCompanion(SimulatedGame simulatedGame, String player, Predicate<PhysicalCard> companionFilter) {
        return simulatedGame.getGame().getGameState().getInPlay().stream()
                .filter(hasCardType(CardType.COMPANION))
                .filter(card -> card.getOwner().equals(player))
                .filter(companionFilter)
                .anyMatch(companion -> simulatedGame.getGame().getGameState().getWounds(companion) > 0);
    }

    public static int getNumberOfMinionsInPlay(SimulatedGame simulatedGame, String player) {
        return (int) simulatedGame.getGame().getGameState().getInPlay().stream()
                .filter(hasCardType(CardType.MINION))
                .filter(card -> card.getOwner().equals(player))
                .count();
    }

    public static int getNumberOfCompanionsInPlay(SimulatedGame simulatedGame, String player) {
        return (int) simulatedGame.getGame().getGameState().getInPlay().stream()
                .filter(hasCardType(CardType.COMPANION))
                .filter(card -> card.getOwner().equals(player))
                .count();
    }
}
