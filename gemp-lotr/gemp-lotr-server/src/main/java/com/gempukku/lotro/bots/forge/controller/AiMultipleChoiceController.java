package com.gempukku.lotro.bots.forge.controller;

import com.gempukku.lotro.bots.BotService;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class AiMultipleChoiceController {
    private final boolean printDebugMessages;
    private final LotroGame game;

    private final AwaitingDecision awaitingDecision;

    private final LotroCardBlueprint sourceBlueprint;
    private final Set<String> options;

    public AiMultipleChoiceController(boolean printDebugMessages, LotroGame game, AwaitingDecision awaitingDecision) throws CardNotFoundException {
        this.printDebugMessages = printDebugMessages;
        this.game = game;
        this.awaitingDecision = awaitingDecision;
        this.sourceBlueprint = BotService.staticLibrary.getLotroCardBlueprint(awaitingDecision.getDecisionParameters().get("source")[0]);
        this.options = new HashSet<>();
        options.addAll(Arrays.asList(awaitingDecision.getDecisionParameters().get("results")));
    }

    public String chooseOption() {
        if (printDebugMessages) {
            System.out.println("Choosing option for " + sourceBlueprint.getFullName());
            System.out.println("Options: " + options);
        }

        if (options.containsAll(List.of("Exert Frodo", "Exert 2 other companions"))) {
            return chooseOptionForMoriaLake();
        }

        if (options.containsAll(List.of("Heal a companion", "Remove a Shadow condition from a companion"))) {
            return chooseOptionForAthelas();
        }
        //TODO extend

        throw new UnsupportedOperationException("Unknown choices: " + awaitingDecision.toJson().toString());
    }

    private String chooseOptionForAthelas() {
        boolean conditionsAttached = game.getGameState()
                .getInPlay().stream().filter(
                        (Predicate<PhysicalCard>) card -> card.getOwner().equals(game.getGameState().getCurrentPlayerId()) && CardType.COMPANION.equals(card.getBlueprint().getCardType()))
                .anyMatch(
                        ((Function<PhysicalCard, Boolean>) card -> game.getGameState().getAttachedCards(card).stream()
                                .anyMatch(card1 -> Side.SHADOW.equals(card1.getBlueprint().getSide()) && CardType.CONDITION.equals(card1.getBlueprint().getCardType())))::apply);

        String chosenOption = conditionsAttached ? "Remove a Shadow condition from a companion" : "Heal a companion";
        if (printDebugMessages) {
            System.out.println(conditionsAttached ? "Found shadow condition attached to companion" : "No attached shadow conditions found");
            System.out.println("Chosen: " + chosenOption);
        }
        return chosenOption;
    }

    private String chooseOptionForMoriaLake() {
        String chosenOption = "Exert Frodo";
        if (printDebugMessages) {
            System.out.println("Always choosing to exert Frodo");
            System.out.println("Chosen: " + chosenOption);
        }
        return chosenOption;
    }
}
