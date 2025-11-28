package com.gempukku.lotro.logic.timing.processes.pregame;

import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.decisions.IntegerAwaitingDecision;
import com.gempukku.lotro.logic.timing.PlayerOrderFeedback;
import com.gempukku.lotro.logic.timing.processes.GameProcess;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class BiddingGameProcess implements GameProcess {
    private final Set<String> _players;
    private final PlayerOrderFeedback _playerOrderFeedback;
    private final Map<String, Integer> _bids = new LinkedHashMap<>();
    private final long _seedToResolveBiddingTie;

    public BiddingGameProcess(Set<String> players, PlayerOrderFeedback playerOrderFeedback, long seedToResolveBiddingTie) {
        _players = players;
        _playerOrderFeedback = playerOrderFeedback;
        _seedToResolveBiddingTie = seedToResolveBiddingTie;
    }

    @Override
    public void process(LotroGame game) {
        for (String player : _players) {
            final String decidingPlayer = player;
            game.getUserFeedback().sendAwaitingDecision(decidingPlayer, new IntegerAwaitingDecision(1, "Choose a number of burdens to bid", 0, -1) {
                @Override
                public void decisionMade(String result) throws DecisionResultInvalidException {
                    try {
                        int bid = Integer.parseInt(result);
                        if (bid < 0)
                            throw new DecisionResultInvalidException();
                        playerPlacedBid(decidingPlayer, bid);
                    } catch (NumberFormatException exp) {
                        throw new DecisionResultInvalidException();
                    }
                }
            });
        }
    }

    private void playerPlacedBid(String playerId, int bid) {
        _bids.put(playerId, bid);
    }

    @Override
    public GameProcess getNextProcess() {
        return new ChooseSeatingOrderGameProcess(_bids, _playerOrderFeedback, _seedToResolveBiddingTie);
    }

    @Override
    public GameProcess copyThisForNewGame(LotroGame game) {
        throw new UnsupportedOperationException("Initial process cannot be copied for new game. Use game replay method instead.");
    }
}
