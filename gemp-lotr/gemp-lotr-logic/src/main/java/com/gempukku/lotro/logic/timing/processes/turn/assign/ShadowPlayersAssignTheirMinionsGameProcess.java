package com.gempukku.lotro.logic.timing.processes.turn.assign;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.PlayOrder;
import com.gempukku.lotro.logic.timing.processes.GameProcess;

import java.util.HashSet;
import java.util.Set;

public class ShadowPlayersAssignTheirMinionsGameProcess implements GameProcess {
    private final GameProcess _followingProcess;
    private final Set<PhysicalCard> _leftoverMinions;
    private PlayOrder _shadowOrder;
    private String _firstShadowPlayer;

    public ShadowPlayersAssignTheirMinionsGameProcess(GameProcess followingProcess, Set<PhysicalCard> leftoverMinions) {
        _followingProcess = followingProcess;
        _leftoverMinions = leftoverMinions;
    }

    @Override
    public void process(LotroGame game) {
        GameState gameState = game.getGameState();
        _shadowOrder = gameState.getPlayerOrder().getCounterClockwisePlayOrder(gameState.getCurrentPlayerId(), false);
        _shadowOrder.getNextPlayer();
        _firstShadowPlayer = _shadowOrder.getNextPlayer();

    }

    @Override
    public GameProcess getNextProcess() {
        return new ShadowPlayerAssignsHisMinionsGameProcess(_shadowOrder, _firstShadowPlayer, _leftoverMinions, _followingProcess);
    }

    @Override
    public GameProcess copyThisForNewGame(LotroGame game) {
        ShadowPlayersAssignTheirMinionsGameProcess copy = new ShadowPlayersAssignTheirMinionsGameProcess(_followingProcess.copyThisForNewGame(game), new HashSet<>(_leftoverMinions));
        if (_shadowOrder != null) {
            copy._shadowOrder = new PlayOrder(_shadowOrder);
            copy._firstShadowPlayer = _firstShadowPlayer;
        }
        return copy;
    }
}
