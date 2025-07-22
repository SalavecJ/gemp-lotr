package com.gempukku.lotro.bots.rl.v2.decisions.arbitrary.general;

import com.gempukku.lotro.bots.rl.v2.decisions.arbitrary.AbstractArbitraryAnswerer;
import com.gempukku.lotro.bots.rl.v2.learning.arbitrary.AbstractArbitraryTrainer;

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

    public static Map<AbstractArbitraryTrainer, AbstractArbitraryAnswerer> generateGeneralArbitraryCardChoicePairs() {
        Map<String, AbstractArbitraryTrainer> trainerMap = new LinkedHashMap<>();
        Map<AbstractArbitraryTrainer, AbstractArbitraryAnswerer> result = new LinkedHashMap<>();

        // Add built-in decisions
        for (String decision : ARBITRARY_DECISIONS) {
            if (!trainerMap.containsKey(decision)) {
                AbstractArbitraryTrainer trainer = new AbstractArbitraryTrainer() {
                    @Override protected String getTextTrigger() { return decision; }
                    @Override public String getName() {
                        return "AcsGeneralTrainer-" + decision;
                    }
                };
                AbstractArbitraryAnswerer answerer = new AbstractArbitraryAnswerer() {
                    @Override protected String getTextTrigger() { return decision; }
                    @Override public String getName() {
                        return "AcsGeneralAnswerer-" + decision;
                    }
                };
                trainerMap.put(decision, trainer);
                result.put(trainer, answerer);
            }
        }

        return result;
    }
}

