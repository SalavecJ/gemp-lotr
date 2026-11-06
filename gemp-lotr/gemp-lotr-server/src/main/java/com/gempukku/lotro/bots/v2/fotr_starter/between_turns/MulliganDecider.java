package com.gempukku.lotro.bots.v2.fotr_starter.between_turns;

import com.gempukku.lotro.bots.v2.BotLogging;
import com.gempukku.lotro.bots.v2.decision.option.MulliganOption;
import com.gempukku.lotro.bots.v2.decision.option.Option;
import com.gempukku.lotro.bots.v2.decision.request.DecisionRequest;
import com.gempukku.lotro.bots.v2.fotr_starter.DeckRecognizer;
import com.gempukku.lotro.bots.v2.fotr_starter.FotrStarterDeck;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.ArrayList;
import java.util.List;

import static com.gempukku.lotro.bots.v2.BotLogging.LogLevel.DEBUG;
import static com.gempukku.lotro.bots.v2.BotLogging.LogLevel.REASONING;

public class MulliganDecider {

    public static Option decide(DecisionRequest decisionRequest, DefaultLotroGame game, String player) {
        if (decisionRequest.getOptions().stream().anyMatch(option -> !(option instanceof MulliganOption))) {
            throw new IllegalArgumentException("Unexpected option type in decision request: " + decisionRequest);
        }

        FotrStarterDeck deck = DeckRecognizer.recognize(game.getGameState().getLotroDeck(player));
        FotrStarterDeck opponentDeck = DeckRecognizer.recognize(game.getGameState().getLotroDeck(game.getGameState().getOpponent(player)));

        List<PhysicalCard> hand = (List<PhysicalCard>) game.getGameState().getHand(player);
        boolean goingFirst = game.getGameState().getFirstPlayerId().equals(player);

        StringBuilder builder = new StringBuilder();
        builder.append("Cards in hand: ");
        for (PhysicalCard physicalCard : hand) {
            builder.append(physicalCard.getBlueprint().getFullName()).append("; ");
        }
        BotLogging.log(REASONING, builder.toString());

        List<String> cardsToMulliganFor = cardsToMulliganFor(deck, opponentDeck, goingFirst);
        BotLogging.log(REASONING, "Want to see in hand: " + String.join("; ", cardsToMulliganFor));

        List<String> foundCardsToMulliganFor = new ArrayList<>();
        for (String cardToMulliganFor : cardsToMulliganFor) {
            if (hand.stream().anyMatch(card -> card.getBlueprint().getFullName().equals(cardToMulliganFor))) {
                foundCardsToMulliganFor.add(cardToMulliganFor);
            }
        }

        boolean mulligan = false;

        if (!foundCardsToMulliganFor.isEmpty()) {
            BotLogging.log(DEBUG, "No mulligan - found cards in hand: " + String.join("; ", foundCardsToMulliganFor));
        } else {
            BotLogging.log(DEBUG, "Mulligan - did not find any of the desired cards in hand");
            mulligan = true;
        }

        for (Option option : decisionRequest.getOptions()) {
            MulliganOption mulliganOption = (MulliganOption) option;
            if (mulliganOption.mulligan() == mulligan) {
                return mulliganOption;
            }
        }

        throw new IllegalArgumentException("No option to " + (mulligan ? "" : "not ") + "mulligan found in decision request: " + decisionRequest);
    }

    public static List<String> cardsToMulliganFor(FotrStarterDeck deck, FotrStarterDeck opponentsDeck, boolean goingFirst) {
        List<String> tbr = new ArrayList<>();

        if (goingFirst) {
            tbr.add("Legolas, Archer of Mirkwood");
            tbr.add("Boromir, Son of Denethor");
            tbr.add("Hobbit Sword");
            if (deck == FotrStarterDeck.ARAGORN_STARTER) {
                tbr.add("Sam, Son of Hamfast");
                if (opponentsDeck == FotrStarterDeck.ARAGORN_STARTER) {
                    tbr.add("Armor");
                }
            }
            if (deck == FotrStarterDeck.GANDALF_STARTER) {
                tbr.add("Gimli, Dwarf of Erebor");
            }
        } else {
            if (deck == FotrStarterDeck.ARAGORN_STARTER) {
                tbr.add("Saruman's Ambition");
                if (opponentsDeck == FotrStarterDeck.GANDALF_STARTER) {
                    tbr.add("Uruk-hai Armory"); // Two times Legolas in enemy deck
                }
            }
            if (deck == FotrStarterDeck.GANDALF_STARTER) {
                tbr.add("Goblin Scimitar");
                tbr.add("They Are Coming");
            }
        }

        return tbr;
    }
}
