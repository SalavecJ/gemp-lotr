package com.gempukku.lotro.logic.timing;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.communication.GameStateListener;
import com.gempukku.lotro.communication.UserFeedback;
import com.gempukku.lotro.game.*;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.state.PreGameInfo;
import com.gempukku.lotro.game.state.actions.DefaultActionsEnvironment;
import com.gempukku.lotro.logic.PlayerOrder;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.ModifiersEnvironment;
import com.gempukku.lotro.logic.modifiers.ModifiersLogic;
import com.gempukku.lotro.logic.modifiers.ModifiersQuerying;
import com.gempukku.lotro.logic.timing.rules.CharacterDeathRule;
import com.gempukku.lotro.logic.vo.LotroDeck;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class DefaultLotroGame implements LotroGame {
    private static final Logger log = LogManager.getLogger(DefaultLotroGame.class);

    private GameState _gameState;
    private final ModifiersLogic _modifiersLogic = new ModifiersLogic();
    private final DefaultActionsEnvironment _actionsEnvironment;
    private final UserFeedback _userFeedback;
    private final TurnProcedure _turnProcedure;
    private final ActionStack _actionStack;
    private boolean _cancelled;
    private boolean _finished;

    private final Adventure _adventure;
    private final LotroFormat _format;

    private final Set<String> _allPlayers;
    private final Map<String, Set<Phase>> _autoPassConfiguration = new HashMap<>();

    private String _winnerPlayerId;
    private final Map<String, String> _losers = new HashMap<>();

    private final Set<GameResultListener> _gameResultListeners = new HashSet<>();

    private final Set<String> _requestedCancel = new HashSet<>();
    private final LotroCardBlueprintLibrary _library;

    // For copying the game
    private final Map<String, LotroDeck> _decks;
    private final long _seed;
    private boolean _saveCheckpoints = false;
    private DefaultLotroGame _checkpointGame = null;

    public DefaultLotroGame(LotroFormat format, Map<String, LotroDeck> decks, UserFeedback userFeedback, final LotroCardBlueprintLibrary library) {
        this(format, decks, userFeedback, library, "No timer", false, "Test Match");
    }

    public DefaultLotroGame(LotroFormat format, Map<String, LotroDeck> decks, UserFeedback userFeedback, final LotroCardBlueprintLibrary library,
                            String timerInfo, boolean allowSpectators, String tournamentName) {
        this(format, decks, userFeedback, library, timerInfo, allowSpectators, tournamentName, new Random().nextLong());
    }

    public DefaultLotroGame(LotroFormat format, Map<String, LotroDeck> decks, UserFeedback userFeedback, final LotroCardBlueprintLibrary library,
            String timerInfo, boolean allowSpectators, String tournamentName, long seed) {
        _library = library;
        _adventure = format.getAdventure();
        _format = format;
        _actionStack = new ActionStack();

        _allPlayers = decks.keySet();

        _actionsEnvironment = new DefaultActionsEnvironment(this, _actionStack);

        final Map<String, List<String>> cards = new LinkedHashMap<>();
        final Map<String, String> ringBearers = new LinkedHashMap<>();
        final Map<String, String> rings = new LinkedHashMap<>();
        final Map<String, String> maps = new LinkedHashMap<>();
        final Map<String, String> notes = new LinkedHashMap<>();
        final StringBuilder formatInfo = new StringBuilder();
        for (String playerId : decks.keySet()) {
            List<String> deck = new LinkedList<>();

            LotroDeck lotroDeck = decks.get(playerId);
            deck.addAll(lotroDeck.getSites());
            deck.addAll(lotroDeck.getDrawDeckCards());

            cards.put(playerId, deck);
            ringBearers.put(playerId, lotroDeck.getRingBearer());
            if (lotroDeck.getRing() != null)
                rings.put(playerId, lotroDeck.getRing());

            if(format.usesMaps()) {
                maps.put(playerId, lotroDeck.getMap());
            }

            var note = "Deck used: <a href='" + lotroDeck.getURL(playerId) + "' target='_blank'>" + lotroDeck.getDeckName() +
                    "</a> [" + lotroDeck.getTargetFormat() + "]<br/><br/>Deck Notes:<br/>";
            if(lotroDeck.getNotes() != null && !lotroDeck.getNotes().equals("null")) {
                note += lotroDeck.getNotes();
            }
            else {
                note += "No deck notes.";
            }

            notes.put(playerId, note);
        }

        if(format.getName().contains("PC")) {
            formatInfo.append(LotroFormat.PCSummary);
        }

        _gameState = new GameState(seed);

        CharacterDeathRule characterDeathRule = new CharacterDeathRule(_actionsEnvironment);
        characterDeathRule.applyRule();

        LotroGame thisGame = this;
        _seed = seed;
        _turnProcedure = new TurnProcedure(this, decks.keySet(), userFeedback, _actionStack,
                new PlayerOrderFeedback() {
                    @Override
                    public void setPlayerOrder(PlayerOrder playerOrder, String firstPlayer) {
                        _gameState.init(playerOrder, firstPlayer, cards, ringBearers, rings, maps, library, format, thisGame);
                    }
                },
                new PregameSetupFeedback() {
                    @Override
                    public void populatePregameInfo() {
                        var preGameInfo = new PreGameInfo(decks.keySet().stream().toList(), tournamentName, timerInfo,
                                !allowSpectators, format, formatInfo.toString(), notes, maps);

                        _gameState.initPreGame(preGameInfo, decks);
                    }
                }, characterDeathRule,
                _seed);
        _userFeedback = userFeedback;

        RuleSet ruleSet = new RuleSet(this, _actionsEnvironment, _modifiersLogic);
        ruleSet.applyRuleSet();

        _adventure.applyAdventureRules(this, _actionsEnvironment, _modifiersLogic);

        // For copying the game
        _decks = decks;
    }

    /**
     * Slow but precise method to copy the game by replaying all decisions.
     */
    public DefaultLotroGame getCopyByReplayingDecisionsFromStart(UserFeedback userFeedback) {
        DefaultLotroGame copy = new DefaultLotroGame(_format, _decks, userFeedback, _library, "No timer", false, "Test Match", _seed);
        userFeedback.setGame(copy);

        // Copies are expected to get copied again to explore different branches
        copy._saveCheckpoints = true;

        copy.startGame();

        List<GameState.DecisionInfo> decisions = _gameState.getDecisionsMade();
        decisions.removeIf(decision -> decision.getAnswer() == null);


        for (int i = 0; i < decisions.size(); i++) {
            GameState.DecisionInfo decision = decisions.get(i);
            AwaitingDecision awaitingDecision = userFeedback.getAwaitingDecision(decision.getPlayer());
            if (awaitingDecision == null) {
                printCopyErrorDetails("No decision pending for player", i, decisions, decision, awaitingDecision, copy, userFeedback);
                throw new IllegalStateException("No decision pending for player " + decision.getPlayer());
            }
            if (decisionsMatch(decision.getDecisionJson(), awaitingDecision.toJson().toString())) {
                String answer = decision.getAnswer();
                userFeedback.participantDecided(decision.getPlayer(), answer);
                try {
                    awaitingDecision.decisionMade(answer);
                } catch (DecisionResultInvalidException e) {
                    printCopyErrorDetails("Decision made in original game is invalid in copied game", i, decisions, decision, awaitingDecision, copy, userFeedback);
                    throw new IllegalStateException("Decision made in original game is invalid in copied game", e);
                }
                copy.carryOutPendingActionsUntilDecisionNeeded();
            } else {
                printCopyErrorDetails("Decisions do not match when copying the game state", i, decisions, decision, awaitingDecision, copy, userFeedback);
                throw new IllegalStateException("Decisions do not match when copying the game state");
            }
        }

        return copy;
    }

    /**
     * Saves a checkpoint of the game state at the start of a new phase.
     * This can be used later to create faster copies of the game by replaying decisions from this checkpoint.
     */
    public void newPhaseProcessAboutToStart() {
        if (_saveCheckpoints) {
            _checkpointGame = new DefaultLotroGame(_format, _decks, null, _library, "No timer", false, "Test Match", _seed);
            _checkpointGame._gameState = new GameState(_gameState, _checkpointGame);
            _checkpointGame._winnerPlayerId = this._winnerPlayerId;
            _checkpointGame._losers.putAll(this._losers);

            _checkpointGame._turnProcedure.updateGameStats();

            if (_winnerPlayerId == null) {
                _checkpointGame._turnProcedure.setGameProcess(_turnProcedure.getGameProcess().copyThisForNewGame(_checkpointGame));
            }
        }
    }

    /**
     * Faster method to copy the game by restoring from the last checkpoint (phase start) and replaying decisions made since then.
     * Assumes that checkpoints were being saved.
     */
    public DefaultLotroGame getCopyByReplayingDecisionsFromLastCheckpoint(UserFeedback userFeedback) {
        if (_checkpointGame == null) {
            throw new IllegalStateException("No checkpoint available for copying the game");
        }

        DefaultLotroGame copy = new DefaultLotroGame(_format, _decks, userFeedback, _library, "No timer", false, "Test Match", _seed);
        userFeedback.setGame(copy);

        // Copies are expected to get copied again to explore different branches
        copy._saveCheckpoints = true;

        // Copy game state from checkpoint
        copy._gameState = new GameState(_checkpointGame._gameState, copy); // Clone the game state

        // Check for winner, if none, copy the rest
        copy._winnerPlayerId = _checkpointGame._winnerPlayerId;
        copy._losers.putAll(_checkpointGame._losers);


        if (_winnerPlayerId == null) {
            // Copy turn procedure and current game process
            copy._turnProcedure.updateGameStats();
            copy._turnProcedure.setGameProcess(_checkpointGame._turnProcedure.getGameProcess().copyThisForNewGame(copy));
            copy.carryOutPendingActionsUntilDecisionNeeded();

            // Play decisions made since checkpoint
            List<GameState.DecisionInfo> decisions = _gameState.getDecisionsMade();
            decisions.removeIf(decision -> decision.getAnswer() == null);
            List<GameState.DecisionInfo> checkpointDecisions = _checkpointGame._gameState.getDecisionsMade();
            checkpointDecisions.removeIf(decision -> decision.getAnswer() == null);

            for (int i = checkpointDecisions.size(); i < decisions.size(); i++) {
                GameState.DecisionInfo decision = decisions.get(i);
                AwaitingDecision awaitingDecision = userFeedback.getAwaitingDecision(decision.getPlayer());
                if (awaitingDecision == null) {
                    printCopyErrorDetails("No decision pending for player", i, decisions, decision, awaitingDecision, copy, userFeedback);
                    throw new IllegalStateException("No decision pending for player " + decision.getPlayer());
                }
                if (decisionsMatch(decision.getDecisionJson(), awaitingDecision.toJson().toString())) {
                    String answer = decision.getAnswer();
                    userFeedback.participantDecided(decision.getPlayer(), answer);
                    try {
                        awaitingDecision.decisionMade(answer);
                    } catch (DecisionResultInvalidException e) {
                        printCopyErrorDetails("Decision made in original game is invalid in copied game", i, decisions, decision, awaitingDecision, copy, userFeedback);
                        throw new IllegalStateException("Decision made in original game is invalid in copied game", e);
                    }
                    copy.carryOutPendingActionsUntilDecisionNeeded();
                } else {
                    printCopyErrorDetails("Decisions do not match when copying the game state", i, decisions, decision, awaitingDecision, copy, userFeedback);
                    throw new IllegalStateException("Decisions do not match when copying the game state");
                }
            }
        }

        return copy;
    }

    /**
     * Compares two decision JSON strings, ignoring the order of elements in arrays.
     * This is necessary because card collections may have different iteration orders
     * but represent the same decision.
     */
    private boolean decisionsMatch(String json1, String json2) {
        // Quick check - if they're exactly equal, we're done
        if (json1.equals(json2)) {
            return true;
        }

        // Parse and normalize the JSON strings by sorting array values
        return normalizeDecisionJson(json1).equals(normalizeDecisionJson(json2));
    }

    /**
     * Normalizes a decision JSON by sorting all array values (like physicalCards, freeCharacters, minions, etc.).
     * This allows order-independent comparison.
     */
    private String normalizeDecisionJson(String json) {
        StringBuilder result = new StringBuilder();
        int pos = 0;

        // Find all arrays in the JSON and sort them
        while (pos < json.length()) {
            int arrayStart = json.indexOf(":[", pos);
            if (arrayStart == -1) {
                // No more arrays, append the rest
                result.append(json.substring(pos));
                break;
            }

            // Append everything up to and including the opening bracket
            result.append(json, pos, arrayStart + 2);

            // Find the closing bracket
            int arrayEnd = json.indexOf(']', arrayStart + 2);
            if (arrayEnd == -1) {
                // Malformed JSON, append the rest as-is
                result.append(json.substring(arrayStart + 2));
                break;
            }

            // Extract array content
            String arrayContent = json.substring(arrayStart + 2, arrayEnd);

            // Only sort if it's a non-empty array with string elements (contains quotes)
            if (!arrayContent.trim().isEmpty() && arrayContent.contains("\"")) {
                // Split by comma and sort
                String[] elements = arrayContent.split(",");
                Arrays.sort(elements);

                // Append sorted elements
                for (int i = 0; i < elements.length; i++) {
                    if (i > 0) result.append(',');
                    result.append(elements[i]);
                }
            } else {
                // Not a string array or empty, keep as-is
                result.append(arrayContent);
            }

            // Move past the closing bracket
            pos = arrayEnd;
        }

        return result.toString();
    }


    @Override
    public boolean shouldAutoPass(String playerId, Phase phase) {
        final Set<Phase> passablePhases = _autoPassConfiguration.get(playerId);
        if (passablePhases == null)
            return false;
        return passablePhases.contains(phase);
    }

    @Override
    public boolean isSolo() {
        return _allPlayers.size() == 1;
    }

    public void addGameResultListener(GameResultListener listener) {
        _gameResultListeners.add(listener);
    }

    public void removeGameResultListener(GameResultListener listener) {
        _gameResultListeners.remove(listener);
    }

    @Override
    public LotroFormat getFormat() {
        return _format;
    }

    public void startGame() {
        if (!_cancelled)
            _turnProcedure.carryOutPendingActionsUntilDecisionNeeded();
    }

    public void carryOutPendingActionsUntilDecisionNeeded() {
        if (!_cancelled)
            _turnProcedure.carryOutPendingActionsUntilDecisionNeeded();
    }

    @Override
    public String getWinnerPlayerId() {
        return _winnerPlayerId;
    }

    public boolean isFinished() {
        return _finished;
    }

    public void cancelGame() {
        if (!_finished) {
            _cancelled = true;

            if (_gameState != null) {
                _gameState.sendMessage("Game was cancelled due to an error, the error was logged and will be fixed soon.");
                _gameState.sendMessage("Please post the replay game link and description of what happened on the TLHH forum.");
            }

            for (GameResultListener gameResultListener : _gameResultListeners)
                gameResultListener.gameCancelled();

            _finished = true;
        }
    }

    public void cancelGameRequested() {
        if (!_finished) {
            _cancelled = true;

            if (_gameState != null)
                _gameState.sendMessage("Game was cancelled, as requested by all parties.");

            for (GameResultListener gameResultListener : _gameResultListeners)
                gameResultListener.gameCancelled();

            _finished = true;
        }
    }

    public boolean isCancelled() {
        return _cancelled;
    }

    @Override
    public void playerWon(String playerId, String reason) {
        if (!_finished) {
            // Any remaining players have lost
            Set<String> losers = new HashSet<>(_allPlayers);
            losers.removeAll(_losers.keySet());
            losers.remove(playerId);

            for (String loser : losers)
                _losers.put(loser, "Other player won");

            gameWon(playerId, reason);
        }
    }

    private void gameWon(String winner, String reason) {
        _winnerPlayerId = winner;

        if (_gameState != null)
            _gameState.sendMessage(_winnerPlayerId + " is the winner due to: " + reason);

        _gameState.finish();

        for (GameResultListener gameResultListener : _gameResultListeners)
            gameResultListener.gameFinished(_winnerPlayerId, reason, _losers);

        _finished = true;
    }

    @Override
    public void playerLost(String playerId, String reason) {
        if (!_finished) {
            if (_losers.get(playerId) == null) {
                _losers.put(playerId, reason);
                if (_gameState != null)
                    _gameState.sendMessage(playerId + " lost due to: " + reason);

                if (_losers.size() + 1 == _allPlayers.size()) {
                    List<String> allPlayers = new LinkedList<>(_allPlayers);
                    allPlayers.removeAll(_losers.keySet());
                    gameWon(allPlayers.getFirst(), "Last remaining player in game");
                }
            }
        }
    }

    public void requestCancel(String playerId) {
        _requestedCancel.add(playerId);
        if (_requestedCancel.size() == _allPlayers.size())
            cancelGameRequested();
    }

    @Override
    public GameState getGameState() {
        return _gameState;
    }

    @Override
    public LotroCardBlueprintLibrary getLotroCardBlueprintLibrary() {
        return _library;
    }

    @Override
    public ActionsEnvironment getActionsEnvironment() {
        return _actionsEnvironment;
    }

    @Override
    public ModifiersEnvironment getModifiersEnvironment() {
        return _modifiersLogic;
    }

    @Override
    public ModifiersQuerying getModifiersQuerying() {
        return _modifiersLogic;
    }

    @Override
    public UserFeedback getUserFeedback() {
        return _userFeedback;
    }

    @Override
    public void checkRingBearerCorruption() {
        GameState gameState = getGameState();
        if (gameState != null && gameState.getCurrentPhase() != Phase.PLAY_STARTING_FELLOWSHIP && gameState.getCurrentPhase() != Phase.BETWEEN_TURNS && gameState.getCurrentPhase() != Phase.PUT_RING_BEARER) {
            // Ring-bearer death
            PhysicalCard ringBearer = gameState.getRingBearer(gameState.getCurrentPlayerId());
            Zone zone = ringBearer.getZone();
            if (zone != null && zone.isInPlay()) {
                // Ring-bearer corruption
                int ringBearerResistance = getModifiersQuerying().getResistance(this, ringBearer);
                if (ringBearerResistance <= 0)
                    playerLost(getGameState().getCurrentPlayerId(), "The Ring-Bearer is corrupted");
            }
        }
    }

    @Override
    public void checkRingBearerAlive() {
        GameState gameState = getGameState();
        if (gameState != null && gameState.getCurrentPhase() != Phase.PLAY_STARTING_FELLOWSHIP && gameState.getCurrentPhase() != Phase.BETWEEN_TURNS && gameState.getCurrentPhase() != Phase.PUT_RING_BEARER) {
            // Ring-bearer death
            PhysicalCard ringBearer = gameState.getRingBearer(gameState.getCurrentPlayerId());
            Zone zone = ringBearer.getZone();
            if (zone == null || !zone.isInPlay())
                playerLost(getGameState().getCurrentPlayerId(), "The Ring-Bearer is dead");
        }
    }

    public void addGameStateListener(String playerId, GameStateListener gameStateListener) {
        _gameState.addGameStateListener(playerId, gameStateListener, _turnProcedure.getGameStats());
    }

    public void removeGameStateListener(GameStateListener gameStateListener) {
        _gameState.removeGameStateListener(gameStateListener);
    }

    public void setPlayerAutoPassSettings(String playerId, Set<Phase> phases) {
        _autoPassConfiguration.put(playerId, phases);
    }

    /**
     * Prints detailed error information when copying the game fails.
     * Shows the current decision, all previous decisions, remaining decisions, and current game state.
     */
    private void printCopyErrorDetails(String errorType, int currentIndex, List<GameState.DecisionInfo> allDecisions,
                                       GameState.DecisionInfo currentDecision, AwaitingDecision awaitingDecision,
                                       DefaultLotroGame copy, UserFeedback userFeedback) {
        System.out.println("=== ERROR in getCopy method: " + errorType + " ===");
        System.out.println("Executing decision " + (currentIndex + 1) + " of " + allDecisions.size());
        System.out.println("Player: " + currentDecision.getPlayer());
        System.out.println("Original: " + currentDecision.getDecisionJson());
        System.out.println("Answer:   " + currentDecision.getAnswer());
        if (awaitingDecision != null) {
            System.out.println("Copy:     " + awaitingDecision.toJson().toString());
        } else {
            System.out.println("Copy:     (no decision pending)");
        }

        List<String> opponent = new ArrayList<>(_allPlayers);
        opponent.remove(currentDecision.getPlayer());
        System.out.println("\nUser feedback pending decisions: " + userFeedback.hasPendingDecisions());

        if (!opponent.isEmpty()) {
            AwaitingDecision opponentDecision = userFeedback.getAwaitingDecision(opponent.getFirst());
            if (opponentDecision != null) {
                System.out.println("Pending decision for opponent " + opponent.getFirst() + ": " + opponentDecision.toJson());
            } else {
                System.out.println("No pending decision for opponent " + opponent.getFirst());
            }
        }

        System.out.println("\nAll previous decisions executed:");
        for (int j = 0; j < currentIndex; j++) {
            GameState.DecisionInfo prevDecision = allDecisions.get(j);
            System.out.println("  [" + (j + 1) + "] Player: " + prevDecision.getPlayer());
            System.out.println("      Decision: " + prevDecision.getDecisionJson());
            System.out.println("      Answer:   " + prevDecision.getAnswer());
        }

        System.out.println("\nRemaining decisions (including current):");
        for (int j = currentIndex; j < Math.min(currentIndex + 10, allDecisions.size()); j++) {
            GameState.DecisionInfo nextDecision = allDecisions.get(j);
            System.out.println("  [" + (j + 1) + "] Player: " + nextDecision.getPlayer());
            System.out.println("      Decision: " + nextDecision.getDecisionJson());
            System.out.println("      Answer:   " + nextDecision.getAnswer());
        }
        if (allDecisions.size() > currentIndex + 10) {
            System.out.println("  ... and " + (allDecisions.size() - currentIndex - 10) + " more decisions");
        }

        System.out.println("\nCurrent game state:");
        System.out.println("  Phase: " + copy.getGameState().getCurrentPhase());
        System.out.println("  Current Player (FP): " + copy.getGameState().getCurrentPlayerId());
        System.out.println("  Current Player (Shadow): " + copy.getGameState().getCurrentShadowPlayer());
        System.out.println("  Turn number: " + copy.getGameState().getTurnNumber());
        System.out.println("  Winner: " + copy.getWinnerPlayerId());

        System.out.println("\n==========================================");
    }
}
