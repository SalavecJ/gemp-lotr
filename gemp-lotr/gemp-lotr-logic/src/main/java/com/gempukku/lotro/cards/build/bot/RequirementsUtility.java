package com.gempukku.lotro.cards.build.bot;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

import java.util.function.Predicate;
import java.util.stream.Stream;

public class RequirementsUtility {
    /*
        DISCARD CHECKS
     */
    public static boolean isInDiscard(LotroGame game, String owner, Predicate<PhysicalCard> predicate) {
        return game.getGameState().getDiscard(owner).stream()
                .anyMatch(predicate);
    }

    public static boolean canBeInDiscardEver(LotroGame game, String owner, Predicate<PhysicalCard> predicate) {
        if (isInDiscard(game, owner, predicate)) {
            return true;
        }
        return Stream.concat(Stream.concat(
                game.getGameState().getHand(owner).stream(),
                game.getGameState().getDeck(owner).stream()),
                game.getGameState().getInPlay().stream().filter((Predicate<PhysicalCard>) card -> card.getOwner().equals(owner)))
                .anyMatch(predicate);
    }

    public static boolean isInDiscard(LotroGame game, String owner, Culture culture, Race race) {
        return  isInDiscard(game, owner, card -> culture.equals(card.getBlueprint().getCulture()) && race.equals(card.getBlueprint().getRace()));
    }

    public static boolean canBeInDiscardEver(LotroGame game, String owner, Culture culture, Race race) {
        return  canBeInDiscardEver(game, owner, card -> culture.equals(card.getBlueprint().getCulture()) && race.equals(card.getBlueprint().getRace()));
    }


    /*
        SPOT CHECKS
     */
    public static boolean canSpot(LotroGame game, String owner, Predicate<PhysicalCard> predicate) {
        return game.getGameState().getInPlay().stream()
                .filter((Predicate<PhysicalCard>) card -> card.getOwner().equals(owner))
                .anyMatch(predicate);

    }

    public static boolean canSpotEver(LotroGame game, String owner, Predicate<PhysicalCard> predicate) {
        // Already can be spotted
        if (canSpot(game, owner, predicate)) {
            return true;
        }

        // Card is in hand or deck, can be spotted later unless dead
        return Stream.concat(game.getGameState().getHand(owner).stream(), game.getGameState().getDeck(owner).stream())
                .filter(card -> {
                    if (!card.getBlueprint().isUnique()) return true;
                    return game.getGameState().getDeadPile(owner).stream()
                            .noneMatch((Predicate<PhysicalCard>) deadCard -> deadCard.getBlueprint().getTitle().equals(card.getBlueprint().getTitle()));
                })
                .anyMatch(predicate);
    }

    public static boolean canSpot(LotroGame game, String owner, String title) {
        return canSpot(game, owner, card -> card.getBlueprint().getTitle().equals(title));
    }

    public static boolean canSpotEver(LotroGame game, String owner, String title) {
        return canSpotEver(game, owner, card -> card.getBlueprint().getTitle().equals(title));
    }

    public static boolean canSpot(LotroGame game, String owner, Race race) {
        return canSpot(game, owner, card -> race.equals(card.getBlueprint().getRace()));
    }

    public static boolean canSpotEver(LotroGame game, String owner, Race race) {
        return canSpotEver(game, owner, card -> race.equals(card.getBlueprint().getRace()));
    }

    public static boolean canSpot(LotroGame game, String owner, CardType cardType) {
        return canSpot(game, owner, card -> cardType.equals(card.getBlueprint().getCardType()));
    }

    public static boolean canSpotEver(LotroGame game, String owner, CardType cardType) {
        return canSpotEver(game, owner, card -> cardType.equals(card.getBlueprint().getCardType()));
    }

    public static boolean canSpotRanger(LotroGame game, String owner) {
        return canSpot(game, owner, card -> game.getModifiersQuerying().hasKeyword(game, card, Keyword.RANGER));
    }

    public static boolean canSpotRangerEver(LotroGame game, String owner) {
        return canSpotEver(game, owner, card -> game.getModifiersQuerying().hasKeyword(game, card, Keyword.RANGER));
    }

    public static boolean canSpot(LotroGame game, String owner, Race race, CardType cardType) {
        return canSpot(game, owner, card -> cardType.equals(card.getBlueprint().getCardType()) && race.equals(card.getBlueprint().getRace()));
    }

    public static boolean canSpotEver(LotroGame game, String owner, Race race, CardType cardType) {
        return canSpotEver(game, owner, card -> cardType.equals(card.getBlueprint().getCardType()) && race.equals(card.getBlueprint().getRace()));
    }

    /*
        EXERT CHECKS
     */
    public static boolean canExert(LotroGame game, String owner, Predicate<PhysicalCard> predicate) {
        return game.getGameState().getInPlay().stream()
                .filter((Predicate<PhysicalCard>) card -> game.getModifiersQuerying().getVitality(game, card) > 1)
                .filter((Predicate<PhysicalCard>) card -> card.getOwner().equals(owner))
                .anyMatch(predicate);
    }

    public static boolean canExertEver(LotroGame game, String owner, Predicate<PhysicalCard> predicate) {
        // Already can exert
        if (canExert(game, owner, predicate)) {
            return true;
        }

        // Check if any in play can be healed
        if (game.getGameState().getInPlay().stream()
                .filter(pc -> pc.getOwner().equals(owner))
                .filter(pc -> canGetHealedEver(game, pc))
                .anyMatch(predicate))
            return true;

        // Check for cards in hand or deck
        return Stream
                .concat(game.getGameState().getHand(owner).stream(),
                        game.getGameState().getDeck(owner).stream())
                .filter(pc -> {
                    boolean dead = game.getGameState().getDeadPile(owner).stream()
                            .anyMatch(deadCard -> deadCard.getBlueprint().getTitle().equals(pc.getBlueprint().getTitle()));
                    return !dead || !pc.getBlueprint().isUnique();
                })
                .anyMatch(predicate);
    }

    public static boolean canExert(LotroGame game, String owner, String title) {
        return canExert(game, owner, card -> card.getBlueprint().getTitle().equals(title));
    }

    public static boolean canExertEver(LotroGame game, String owner, String title) {
        return canExertEver(game, owner, card -> card.getBlueprint().getTitle().equals(title));
    }

    public static boolean canExert(LotroGame game, String owner, Race race) {
        return canExert(game, owner, card -> race.equals(card.getBlueprint().getRace()));
    }

    public static boolean canExertEver(LotroGame game, String owner, Race race) {
        return canExertEver(game, owner, card -> race.equals(card.getBlueprint().getRace()));
    }

    public static boolean canExertRanger(LotroGame game, String owner) {
        return canExert(game, owner, card -> game.getModifiersQuerying().hasKeyword(game, card, Keyword.RANGER));
    }

    public static boolean canExertRangerEver(LotroGame game, String owner) {
        return canExertEver(game, owner, card -> game.getModifiersQuerying().hasKeyword(game, card, Keyword.RANGER));
    }

    public static boolean canExert(LotroGame game, String owner, Race race, CardType cardType) {
        return canExert(game, owner, card -> cardType.equals(card.getBlueprint().getCardType()) && race.equals(card.getBlueprint().getRace()));
    }

    public static boolean canExertEver(LotroGame game, String owner, Race race, CardType cardType) {
        return canExertEver(game, owner, card -> cardType.equals(card.getBlueprint().getCardType()) && race.equals(card.getBlueprint().getRace()));
    }

    /*
        HEALING CHECKS
     */
    private static boolean canGetHealedEver(LotroGame game, PhysicalCard card) {
        // Sanctuary healing possible at sites 3 and 6
        int site = game.getGameState().getPlayerPosition(card.getOwner());
        if (site <= 6 && CardType.COMPANION.equals(card.getBlueprint().getCardType())) return true;

        // Can get healed by discarding the same character from hand if unique companion
        boolean canGetHealedByDiscard = Stream.concat(game.getGameState().getHand(card.getOwner()).stream(), game.getGameState().getDeck(card.getOwner()).stream())
                .filter(physicalCard -> CardType.COMPANION.equals(physicalCard.getBlueprint().getCardType()))
                .filter(physicalCard -> physicalCard.getBlueprint().getTitle().equals(card.getBlueprint().getTitle()))
                .anyMatch(physicalCard -> physicalCard.getBlueprint().isUnique());
        if (canGetHealedByDiscard) return true;

        // TODO check for healing effects when abilities get implemented

        return false;
    }
}
