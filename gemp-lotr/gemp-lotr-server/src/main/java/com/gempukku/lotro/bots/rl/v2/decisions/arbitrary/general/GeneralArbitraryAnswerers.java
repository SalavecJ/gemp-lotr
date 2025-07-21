package com.gempukku.lotro.bots.rl.v2.decisions.arbitrary.general;

import com.gempukku.lotro.bots.BotService;
import com.gempukku.lotro.bots.rl.v2.decisions.arbitrary.AbstractArbitraryAnswerer;
import com.gempukku.lotro.bots.rl.v2.learning.arbitrary.AbstractArbitraryTrainer;
import com.gempukku.lotro.game.LotroCardBlueprint;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GeneralArbitraryAnswerers {
    private static final List<String> ARBITRARY_DECISIONS = List.of(
            "Choose card from deck to put on top of deck",
            "Choose card from deck to put beneath draw deck",
            "Choose card from discard to put beneath draw deck",
            "Choose card from discard to put on top of deck",
            "Choose card from hand to put beneath draw deck",
            "Choose card from hand to put on top of deck",
            "Choose card from play to put beneath draw deck",
            "Choose card from play to put on top of deck",
            "Choose a card to put on top of the deck",
            "Choose card to play from hand",
            "Choose cards from hand to discard",
            "Choose cards to exchange in hand",
            "Choose cards to exchange in dead pile",
            "Choose cards to exchange in discard",
            "Choose card to exchange stacked cards from",
            "Choose cards to reveal",
            "Choose cards to shuffle into the draw deck",
            "Choose cards to stack",
            "Choose card from dead pile",
            "Choose cards from deck",
            "Choose card from discard",
            "Choose stacked card",
            "Choose cards to remove"
    );

    public static Map<AbstractArbitraryTrainer, AbstractArbitraryAnswerer> generateUniqueArbitraryCardChoicePairs() {
        Map<String, AbstractArbitraryTrainer> trainerMap = new LinkedHashMap<>();
        Map<AbstractArbitraryTrainer, AbstractArbitraryAnswerer> result = new LinkedHashMap<>();

        // Add built-in decisions
        for (String decision : ARBITRARY_DECISIONS) {
            if (!trainerMap.containsKey(decision)) {
                AbstractArbitraryTrainer trainer = new AbstractArbitraryTrainer() {
                    @Override protected String getTextTrigger() { return decision; }
                    @Override public String getName() {
                        return "Trainer-" + decision;
                    }
                };
                AbstractArbitraryAnswerer answerer = new AbstractArbitraryAnswerer() {
                    @Override protected String getTextTrigger() { return decision; }
                    @Override public String getName() {
                        return "Answerer-" + decision;
                    }
                };
                trainerMap.put(decision, trainer);
                result.put(trainer, answerer);
            }
        }

        // Add JSON-based decisions
        Map<String, LotroCardBlueprint> allBlueprints = BotService.staticLibrary.getBaseCards();
        for (LotroCardBlueprint blueprint : allBlueprints.values()) {
            JSONObject json = blueprint.getJsonDefinition();
            collectArbitraryTextRecursively(json, trainerMap, result);
        }

        return result;
    }

    private static void collectArbitraryTextRecursively(Object node,
                                                        Map<String, AbstractArbitraryTrainer> trainerMap,
                                                        Map<AbstractArbitraryTrainer, AbstractArbitraryAnswerer> result) {
        if (node instanceof JSONObject jsonObject) {
            Object type = jsonObject.get("type");
            Object text = jsonObject.get("text");
            if ("chooseArbitraryCards".equals(type) && text instanceof String textStr) {
                if (!trainerMap.containsKey(textStr)) {
                    AbstractArbitraryTrainer trainer = new AbstractArbitraryTrainer() {
                        @Override protected String getTextTrigger() { return textStr; }
                        @Override public String getName() {
                            return "Trainer-" + textStr;
                        }
                    };
                    AbstractArbitraryAnswerer answerer = new AbstractArbitraryAnswerer() {
                        @Override protected String getTextTrigger() { return textStr; }
                        @Override public String getName() {
                            return "Answerer-" + textStr;
                        }
                    };
                    trainerMap.put(textStr, trainer);
                    result.put(trainer, answerer);
                }
            }

            for (Object value : jsonObject.values()) {
                collectArbitraryTextRecursively(value, trainerMap, result);
            }
        } else if (node instanceof JSONArray array) {
            for (Object element : array) {
                collectArbitraryTextRecursively(element, trainerMap, result);
            }
        }
    }
}

