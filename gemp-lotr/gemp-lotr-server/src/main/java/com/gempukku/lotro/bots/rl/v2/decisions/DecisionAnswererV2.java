package com.gempukku.lotro.bots.rl.v2.decisions;

import com.gempukku.lotro.bots.rl.v2.ModelRegistryV2;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

public interface DecisionAnswererV2 {
    boolean appliesTo(GameState gameState, AwaitingDecision decision, String playerName);
    String getAnswer(GameState gameState, AwaitingDecision decision, String playerName, ModelRegistryV2 modelRegistry);

    String getName();

     class ScoredCard {
        public String cardId;
        public double score;

        public ScoredCard(String cardId, double score) {
            this.cardId = cardId;
            this.score = score;
        }

        @Override
        public String toString() {
            return "ScoredCard{" +
                    "cardId='" + cardId + '\'' +
                    ", score=" + score +
                    '}';
        }
    }
}
