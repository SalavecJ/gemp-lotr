package com.gempukku.lotro.cards.evaluation;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.state.GameState;

import java.util.HashMap;

public class CardEvaluators {
   private static final HashMap<String, CardEvaluator> EVALUATORS = new HashMap<>();

    private CardEvaluators() {

    }

    public static boolean doesAnythingIfPlayed(GameState gameState, int physicalId, String playerName, LotroCardBlueprint blueprint) {
        if (blueprint.getCardType() != CardType.EVENT) {
            return true;
        } else {
            if (EVALUATORS.containsKey(blueprint.getId())) {
                return EVALUATORS.get(blueprint.getId()).doesAnythingIfPlayed(gameState, physicalId, playerName);
            } else {
                throw new IllegalArgumentException("Unknown blueprint id: " + blueprint.getId());
            }
        }
    }
}
