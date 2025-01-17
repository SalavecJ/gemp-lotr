package com.gempukku.lotro.tournament;

import com.gempukku.lotro.DateUtils;
import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.db.vo.CollectionType;

import java.util.Date;

public class ScheduledTournamentQueue extends AbstractTournamentQueue implements TournamentQueue {
    private static final long _signupTimeBeforeStart = 1000 * 60 * 60; // 60 minutes before start
    private final long _startTime;
    private final int _minimumPlayers;
    private final String _startCondition;
    private final TournamentService _tournamentService;
    private final String _tournamentName;
    private final CollectionType _collectionType;
    private final Tournament.Stage _stage;
    private final String _scheduledTournamentId;

    public ScheduledTournamentQueue(String scheduledTournamentId, int cost, boolean requiresDeck, TournamentService tournamentService, long startTime,
                                    String tournamentName, String format, CollectionType collectionType, Tournament.Stage stage,
                                    PairingMechanism pairingMechanism, TournamentPrizes tournamentPrizes, int minimumPlayers) {
        super(cost, requiresDeck, collectionType, tournamentPrizes, pairingMechanism, format);
        _scheduledTournamentId = scheduledTournamentId;
        _tournamentService = tournamentService;
        _startTime = startTime;
        _minimumPlayers = minimumPlayers;
        _startCondition = DateUtils.formatDateWithHour(new Date(_startTime));
        _tournamentName = tournamentName;
        _collectionType = collectionType;
        _stage = stage;
    }

    @Override
    public String getTournamentQueueName() {
        return _tournamentName;
    }

    @Override
    public String getPairingDescription() {
        return _pairingMechanism.getPlayOffSystem() + ", minimum players: " + _minimumPlayers;
    }

    @Override
    public String getStartCondition() {
        return _startCondition;
    }

    @Override
    public synchronized boolean process(TournamentQueueCallback tournamentQueueCallback, CollectionsManager collectionsManager) {
        long now = System.currentTimeMillis();
        if (now > _startTime) {
            if (_players.size() >= _minimumPlayers) {

                for (String player : _players)
                    _tournamentService.addPlayer(_scheduledTournamentId, player, _playerDecks.get(player));

                Tournament tournament = _tournamentService.addTournament(_scheduledTournamentId, null, _tournamentName, _format, _collectionType, _stage,
                        _pairingMechanism.getRegistryRepresentation(), _tournamentPrizes.getRegistryRepresentation(), new Date());
                tournamentQueueCallback.createTournament(tournament);
            } else {
                _tournamentService.updateScheduledTournamentStarted(_scheduledTournamentId);
                leaveAllPlayers(collectionsManager);
            }

            return true;
        }
        return false;
    }

    @Override
    public boolean isJoinable() {
        return System.currentTimeMillis() >= _startTime - _signupTimeBeforeStart;
    }
}
