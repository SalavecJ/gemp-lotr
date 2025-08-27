package com.gempukku.lotro.bots.forge.controller;

import com.gempukku.lotro.bots.BotService;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

import java.util.*;

public class AiIntegerChoiceController {
    private final boolean printDebugMessages;
    private final LotroGame game;

    private final AwaitingDecision awaitingDecision;
    private final int min;
    private final int max;
    private final LotroCardBlueprint sourceBlueprint;

    private Set<String> chooseMax = new HashSet<>();

    public AiIntegerChoiceController(boolean printDebugMessages, LotroGame game, AwaitingDecision awaitingDecision) throws CardNotFoundException {
        this.printDebugMessages = printDebugMessages;
        this.game = game;
        this.awaitingDecision = awaitingDecision;
        this.min = Integer.parseInt(awaitingDecision.getDecisionParameters().get("min")[0]);
        this.max = Integer.parseInt(awaitingDecision.getDecisionParameters().get("max")[0]);
        this.sourceBlueprint = BotService.staticLibrary.getLotroCardBlueprint(awaitingDecision.getDecisionParameters().get("source")[0]);


        initializeSets();
    }

    private void initializeSets() {
        chooseMax.add("1_145"); // Uruk Brood
    }

    public int chooseNumber() {
        if (printDebugMessages) {
            System.out.println("Choosing number for " + sourceBlueprint.getFullName());
            System.out.println("Min " + min + "; Max " + max);
        }

        if (chooseMax.contains(sourceBlueprint.getId())) {
            if (printDebugMessages) {
                System.out.println("Choose max strategy used");
                System.out.println("Chosen: " + max);
            }
            return max;
        }

        throw new UnsupportedOperationException("Unknown integer choice strategy for: " + awaitingDecision.toJson().toString());
    }
}
