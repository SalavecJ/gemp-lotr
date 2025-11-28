package com.gempukku.lotro.logic.timing.processes.pregame;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.PlayOrder;
import com.gempukku.lotro.logic.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.lotro.logic.timing.processes.GameProcess;
import com.gempukku.lotro.logic.timing.processes.turn.EndOfTurnGameProcess;

import java.util.LinkedHashSet;
import java.util.Set;

public class MulliganProcess implements GameProcess {
    private final PlayOrder _playOrder;

    private GameProcess _nextProcess;

    public MulliganProcess(PlayOrder playOrder) {
        _playOrder = playOrder;
    }

    @Override
    public void process(final LotroGame game) {
        final int handSize = game.getFormat().getHandSize();

        final String nextPlayer = _playOrder.getNextPlayer();
        if (nextPlayer != null) {
            game.getUserFeedback().sendAwaitingDecision(nextPlayer,
                    new MultipleChoiceAwaitingDecision(1, "Do you wish to mulligan? (Shuffle cards back and draw " + (handSize - 2) + ")", new String[]{"No", "Yes"}, -1) {
                        @Override
                        protected void validDecisionMade(int index, String result) {
                            if (index == 1) {
                                final GameState gameState = game.getGameState();
                                gameState.sendMessage(nextPlayer + " mulligans");
                                Set<PhysicalCard> hand = new LinkedHashSet<>(gameState.getHand(nextPlayer));
                                gameState.removeCardsFromZone(nextPlayer, hand);
                                for (PhysicalCard card : hand)
                                    gameState.addCardToZone(game, card, Zone.DECK);

                                gameState.shuffleDeck(nextPlayer);
                                for (int i = 0; i < handSize - 2; i++)
                                    gameState.playerDrawsCard(nextPlayer);
                            } else {
                                game.getGameState().sendMessage(nextPlayer + " decides not to mulligan");
                            }
                        }
                    });
            _nextProcess = new MulliganProcess(_playOrder);
        } else {
            _nextProcess = new EndOfTurnGameProcess();
        }
    }

    @Override
    public GameProcess getNextProcess() {
        return _nextProcess;
    }

    @Override
    public GameProcess copyThisForNewGame(LotroGame game) {
        MulliganProcess copy = new MulliganProcess(new PlayOrder(_playOrder));
        if (_nextProcess != null)
            copy._nextProcess = _nextProcess.copyThisForNewGame(game);
        return copy;
    }
}
