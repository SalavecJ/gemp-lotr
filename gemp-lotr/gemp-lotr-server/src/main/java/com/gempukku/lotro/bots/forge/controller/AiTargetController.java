package com.gempukku.lotro.bots.forge.controller;

import com.gempukku.lotro.bots.forge.cards.BotTargetingMode;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

import java.util.*;

public class AiTargetController {

    private final boolean printDebugMessages;

    private final List<PhysicalCard> options;
    private final PhysicalCard source;
    private final AwaitingDecision awaitingDecision;
    private final int min;
    private final int max;

    public AiTargetController(boolean printDebugMessages, List<PhysicalCard> options,
                              PhysicalCard source, AwaitingDecision awaitingDecision) {
        this.printDebugMessages = printDebugMessages;
        this.options = options;
        this.source = source;
        this.awaitingDecision = awaitingDecision;
        this.min = Integer.parseInt(awaitingDecision.getDecisionParameters().get("min")[0]);
        this.max = Integer.parseInt(awaitingDecision.getDecisionParameters().get("max")[0]);

    }

    public List<PhysicalCard> chooseTarget() {
        if (printDebugMessages) {
            System.out.println("Choosing target for " + source.getBlueprint().getFullName());
            StringBuilder builder = new StringBuilder("Options: ");
            for (PhysicalCard option : options) {
                builder.append(option.getBlueprint().getFullName()).append("; ");
            }
            System.out.println(builder);
        }

        BotTargetingMode mode;
        try {
            mode = BotTargetingMode.RANDOM;
        } catch (IllegalStateException e) {
            throw new UnsupportedOperationException("Decision not supported: " + awaitingDecision.toJson().toString());
        }

        if (printDebugMessages) {
            System.out.println("Targeting mode: " + mode);
            if (min != max) {
                System.out.println("Min options to choose: " + min);
                System.out.println("Max options to choose: " + max);
            } else {
                System.out.println("Options to choose: " + min);
            }
        }

        List<PhysicalCard> tbr = new ArrayList<>();
        for (int i = 1; i <= min; i++) {
            // As a basic implementation, choose highest strength target
            PhysicalCard chosen = chooseTargetRandom();
            tbr.add(chosen);
            options.remove(chosen);
        }

        return tbr;
    }

    private PhysicalCard chooseTargetRandom() {
        PhysicalCard chosen = options.get(new Random().nextInt(0, options.size()));
        if (printDebugMessages) {
            System.out.println("Chosen: " + chosen.getBlueprint().getFullName());
        }
        return chosen;
    }
}
