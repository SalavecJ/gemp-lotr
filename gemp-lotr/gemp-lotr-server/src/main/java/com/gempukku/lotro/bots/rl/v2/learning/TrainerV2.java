package com.gempukku.lotro.bots.rl.v2.learning;

import com.gempukku.lotro.bots.rl.v2.ModelRegistryV2;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import smile.classification.SoftClassifier;

import java.util.List;

public interface TrainerV2 {
    boolean appliesTo(GameState gameState, AwaitingDecision decision, String playerName);

    String getAnswer(LotroGame game, AwaitingDecision decision, String playerName, ModelRegistryV2 modelRegistry);

    SoftClassifier<double[]> train(List<SavedVector> vectors);

    List<SavedVector> toStringVectors(LotroGame game, AwaitingDecision decision, String playerId, String answer);

    String getName();

    static boolean equal(TrainerV2 t1, TrainerV2 t2) {
        return t1.getClass().equals(t2.getClass()) || t1.getName().equals(t2.getName());
    }

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
