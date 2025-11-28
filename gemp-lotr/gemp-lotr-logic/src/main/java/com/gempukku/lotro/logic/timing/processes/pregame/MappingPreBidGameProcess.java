package com.gempukku.lotro.logic.timing.processes.pregame;

import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.timing.PlayerOrderFeedback;
import com.gempukku.lotro.logic.timing.PregameSetupFeedback;
import com.gempukku.lotro.logic.timing.processes.GameProcess;

import java.util.Set;

public class MappingPreBidGameProcess implements GameProcess {
    private final Set<String> _players;
    private final PlayerOrderFeedback _playerOrderFeedback;
    private final PregameSetupFeedback _pregameSetupFeedback;
    private final long _seedToResolveBiddingTie;

    public MappingPreBidGameProcess(Set<String> players, PlayerOrderFeedback playerOrderFeedback,
            PregameSetupFeedback pregameSetupFeedback, long seedToResolveBiddingTie) {
        _players = players;
        _playerOrderFeedback = playerOrderFeedback;
        _pregameSetupFeedback = pregameSetupFeedback;
        _seedToResolveBiddingTie = seedToResolveBiddingTie;
    }

    @Override
    public void process(LotroGame game) {
        _pregameSetupFeedback.populatePregameInfo();
    }


    @Override
    public GameProcess getNextProcess() {
        return new BiddingGameProcess(_players, _playerOrderFeedback, _seedToResolveBiddingTie);
    }

    @Override
    public GameProcess copyThisForNewGame(LotroGame game) {
        throw new UnsupportedOperationException("Initial process cannot be copied for new game. Use game replay method instead.");
    }
}
