package com.gempukku.lotro.bots.forge.controller;

import com.gempukku.lotro.bots.BotService;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

import java.util.*;

public class AiRequiredResponseOrderController {
    private final boolean printDebugMessages;
    private final LotroGame game;
    private final Set<ResponseOption> options;
    private final Map<String, Integer> priorityMap = new HashMap<>();

    public AiRequiredResponseOrderController(boolean printDebugMessages, LotroGame game, AwaitingDecision awaitingDecision) throws CardNotFoundException {
        this.printDebugMessages = printDebugMessages;
        this.game = game;
        this.options = new HashSet<>();

        for (int i = 0; i < awaitingDecision.getDecisionParameters().get("actionId").length; i++) {
            options.add(new ResponseOption(
                    awaitingDecision.getDecisionParameters().get("actionId")[i],
                    awaitingDecision.getDecisionParameters().get("blueprintId")[i],
                    awaitingDecision.getDecisionParameters().get("actionText")[i]
            ));
        }

        initializePriorityMap();
    }

    private void initializePriorityMap() {
        priorityMap.put("1_150", 0); // Uruk Rager
        priorityMap.put("1_350", 0); // Dimrill Dale
        priorityMap.put("1_178", 0); // Goblin Runner
        priorityMap.put("1_191", 0); // Moria Scout
    }

    public int chooseOption() {
        if (printDebugMessages) {
            System.out.println("Choosing order of required options");
            System.out.println("Options: ");
            for (ResponseOption option : options) {
                System.out.println(option.text);
            }
        }

        ResponseOption chosenOption = options.stream().max(Comparator.comparingInt(ResponseOption::getPriority)).orElseThrow();

        if (printDebugMessages) {
            System.out.println("Chosen: " + chosenOption.text);
        }

        return chosenOption.id;
    }

    private class ResponseOption {
        int id;
        LotroCardBlueprint source;
        String text;

        public ResponseOption(String id, String source, String text) throws CardNotFoundException {
            this.id = Integer.parseInt(id);
            if (source.equals("rules")) {
                this.source = null;
            } else {
                this.source = BotService.staticLibrary.getLotroCardBlueprint(source);
            }
            this.text = text;
        }

        int getPriority() {
            if (source == null) {
                return 0;
            }
            if (priorityMap.containsKey(source.getId())) {
                return priorityMap.get(source.getId());
            }
            throw new UnsupportedOperationException("Unknown response priority for card: " + source.getFullName());
        }
    }
}
