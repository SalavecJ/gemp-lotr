package com.gempukku.lotro.bots.v2.fotr_starter.utility;

import com.gempukku.lotro.bots.v2.fotr_starter.DeckRecognizer;
import com.gempukku.lotro.bots.v2.fotr_starter.FotrStarterDeck;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.ArrayList;
import java.util.List;

public class DeadCardEvaluator {

    public static List<PhysicalCard> getDeadCards(DefaultLotroGame game, String player) {
        List<PhysicalCard> hand = (List<PhysicalCard>) game.getGameState().getHand(player);
        List<PhysicalCard> deadCards = new ArrayList<>();

        for (PhysicalCard card : hand) {
            if (isDead(game, player, card)) {
                deadCards.add(card);
            }
        }

        return deadCards;
    }

    public static boolean isDead(DefaultLotroGame game, String player, PhysicalCard card) {
        List<PhysicalCard> deadPile = (List<PhysicalCard>) game.getGameState().getDeadPile(player);
        boolean gimliDead = false;
        boolean legolasDead = false;
        boolean gandalfDead = false;
        boolean aragornDead = false;
        boolean boromirDead = false;
        boolean frodoDead = false;
        boolean samDead = false;
        for (PhysicalCard deadCard : deadPile) {
            if (deadCard.getBlueprint().getTitle().equals("Gimli")) {
                gimliDead = true;
            } else if (deadCard.getBlueprint().getTitle().equals("Legolas")) {
                legolasDead = true;
            } else if (deadCard.getBlueprint().getTitle().equals("Gandalf")) {
                gandalfDead = true;
            } else if (deadCard.getBlueprint().getTitle().equals("Aragorn")) {
                aragornDead = true;
            } else if (deadCard.getBlueprint().getTitle().equals("Boromir")) {
                boromirDead = true;
            } else if (deadCard.getBlueprint().getTitle().equals("Frodo")) {
                frodoDead = true;
            } else if (deadCard.getBlueprint().getTitle().equals("Sam")) {
                samDead = true;
            }
        }

        // companions are dead in hand if another copy is in dead pile already
        if (card.getBlueprint().getCardType() == CardType.COMPANION) {
            for (PhysicalCard deadCard : deadPile) {
                if (deadCard.getBlueprint().getTitle().equals(card.getBlueprint().getTitle())) {
                    return true;
                }
            }
        }

        // Their Halls of Stone event is dead if Gimli is in dead pile
        if (card.getBlueprint().getFullName().equals("Their Halls of Stone")) {
            if (gimliDead) {
                return true;
            }
        }

        // Defiance is dead if Legolas is in dead pile
        if (card.getBlueprint().getFullName().equals("Defiance")) {
            if (legolasDead) {
                return true;
            }
        }

        // Intimidate, Mysterious Wizard, Sleep, Caradhras, and Treachery Deeper Than You Know are dead if Gandalf is in dead pile
        if (card.getBlueprint().getFullName().equals("Intimidate") ||
            card.getBlueprint().getFullName().equals("Mysterious Wizard") ||
            card.getBlueprint().getFullName().equals("Sleep, Caradhras") ||
            card.getBlueprint().getFullName().equals("Treachery Deeper Than You Know")) {
            if (gandalfDead) {
                return true;
            }
        }

        // Noble Intentions is dead if all Gimli, Legolas, and Gandalf are in dead pile
        if (card.getBlueprint().getFullName().equals("Noble Intentions")) {
            if (gimliDead && legolasDead && gandalfDead) {
                return true;
            }
        }

        // Armor, Athelas, Coat of Mail, and Great Shield are dead if both Aragorn and Boromir are in dead pile
        if (card.getBlueprint().getFullName().equals("Armor") ||
            card.getBlueprint().getFullName().equals("Athelas") ||
            card.getBlueprint().getFullName().equals("Coat of Mail") ||
            card.getBlueprint().getFullName().equals("Great Shield")) {
            if (aragornDead && boromirDead) {
                return true;
            }
        }

        // Armor is dead if either Aragorn or Boromir is in dead pile and the other is in play bearing Coat of Mail
        if (card.getBlueprint().getFullName().equals("Armor")) {
            if ((aragornDead && game.getGameState().getInPlay().stream().anyMatch(c -> c.getBlueprint().getTitle().equals("Boromir") && c.getOwner().equals(player) && hasAttached(game, c, "Coat of Mail"))) ||
                (boromirDead && game.getGameState().getInPlay().stream().anyMatch(c -> c.getBlueprint().getTitle().equals("Aragorn") && c.getOwner().equals(player) && hasAttached(game, c, "Coat of Mail")))) {
                return true;
            }
        }

        // Coat of Mail is dead if either Aragorn or Boromir is in dead pile and the other is in play bearing Armor
        if (card.getBlueprint().getFullName().equals("Coat of Mail")) {
            if ((aragornDead && game.getGameState().getInPlay().stream().anyMatch(c -> c.getBlueprint().getTitle().equals("Boromir") && c.getOwner().equals(player) && hasAttached(game, c, "Armor"))) ||
                (boromirDead && game.getGameState().getInPlay().stream().anyMatch(c -> c.getBlueprint().getTitle().equals("Aragorn") && c.getOwner().equals(player) && hasAttached(game, c, "Armor")))) {
                return true;
            }
        }

        // Great Shield is dead if either Aragorn or Boromir is in dead pile and the other is in play already bearing Great Shield
        if (card.getBlueprint().getFullName().equals("Great Shield")) {
            if ((aragornDead && game.getGameState().getInPlay().stream().anyMatch(c -> c.getBlueprint().getTitle().equals("Boromir") && c.getOwner().equals(player) && hasAttached(game, c, "Great Shield"))) ||
                (boromirDead && game.getGameState().getInPlay().stream().anyMatch(c -> c.getBlueprint().getTitle().equals("Aragorn") && c.getOwner().equals(player) && hasAttached(game, c, "Great Shield")))) {
                return true;
            }
        }

        // Hobbit Sword is dead if either Frodo or Sam is in dead pile and the other is in play bearing Hobbit Sword
        if (card.getBlueprint().getFullName().equals("Hobbit Sword")) {
            if ((frodoDead && game.getGameState().getInPlay().stream().anyMatch(c -> c.getBlueprint().getTitle().equals("Sam") && c.getOwner().equals(player) && hasAttached(game, c, "Hobbit Sword"))) ||
                (samDead && game.getGameState().getInPlay().stream().anyMatch(c -> c.getBlueprint().getTitle().equals("Frodo") && c.getOwner().equals(player) && hasAttached(game, c, "Hobbit Sword")))) {
                return true;
            }
        }

        // Eregion's Trails, Gondor's Vengeance, Pathfinder, Swordsman of the Northern Kingdom, and No Stranger to the Shadows are dead if Aragorn is in dead pile
        if (card.getBlueprint().getFullName().equals("Eregion's Trails") ||
            card.getBlueprint().getFullName().equals("Gondor's Vengeance") ||
            card.getBlueprint().getFullName().equals("Pathfinder") ||
            card.getBlueprint().getFullName().equals("Swordsman of the Northern Kingdom") ||
            card.getBlueprint().getFullName().equals("No Stranger to the Shadows")) {
            if (aragornDead) {
                return true;
            }
        }

        // Eregion's Trails is dead if on site 5+ against Aragorn deck, or on site 4+ against Gandalf deck
        FotrStarterDeck opponentDeck = DeckRecognizer.recognize(game.getGameState().getLotroDeck(game.getGameState().getOpponent(player)));
        if (card.getBlueprint().getFullName().equals("Eregion's Trails")) {
            if ((opponentDeck == FotrStarterDeck.ARAGORN_STARTER && game.getGameState().getCurrentSiteNumber() >= 5) ||
                (opponentDeck == FotrStarterDeck.GANDALF_STARTER && game.getGameState().getCurrentSiteNumber() >= 4)) {
                return true;
            }
        }

        // No Stranger to the Shadows is dead if Aragorn is already bearing one copy of it
        if (card.getBlueprint().getFullName().equals("No Stranger to the Shadows")) {
            if (game.getGameState().getInPlay().stream().anyMatch(c -> c.getBlueprint().getTitle().equals("Aragorn") && c.getOwner().equals(player) && hasAttached(game, c, "No Stranger to the Shadows"))) {
                return true;
            }
        }

        //  Rosie Cotton is dead if Sam is in dead pile
        if (card.getBlueprint().getFullName().equals("Rosie Cotton, Hobbiton Lass")) {
            if (samDead) {
                return true;
            }
        }

        return false;
    }

    private static boolean hasAttached(DefaultLotroGame game, PhysicalCard card, String attachedCardFullName) {
        return game.getGameState().getAttachedCards(card).stream().anyMatch(a -> a.getBlueprint().getFullName().equals(attachedCardFullName));
    }
}
