package com.gempukku.lotro.logic.actions;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.decisions.CardsSelectionDecision;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.effects.DiscardCardsFromHandEffect;
import com.gempukku.lotro.logic.effects.DrawCardsEffect;
import com.gempukku.lotro.logic.effects.PlayoutDecisionEffect;
import com.gempukku.lotro.logic.effects.TriggeringResultEffect;
import com.gempukku.lotro.logic.timing.Action;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.results.ReconcileResult;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class PlayerReconcilesAction implements Action {
    private final LotroGame _game;
    private final String _playerId;

    private Queue<Effect> _effectQueue;

    public PlayerReconcilesAction(LotroGame game, String playerId) {
        _game = game;
        _playerId = playerId;
    }

    @Override
    public Type getType() {
        return Type.RECONCILE;
    }

    @Override
    public void setVirtualCardAction(boolean virtualCardAction) {
    }

    @Override
    public boolean isVirtualCardAction() {
        return false;
    }

    @Override
    public Phase getActionTimeword() {
        return null;
    }

    @Override
    public void setActionTimeword(Phase phase) {
    }

    @Override
    public String getPerformingPlayer() {
        return null;
    }

    @Override
    public void setPerformingPlayer(String playerId) {
    }

    @Override
    public PhysicalCard getActionSource() {
        return null;
    }

    @Override
    public PhysicalCard getActionAttachedToCard() {
        return null;
    }

    @Override
    public String getText(LotroGame game) {
        return "Player reconciles";
    }

    @Override
    public Effect nextEffect(LotroGame game) {
        if (_effectQueue == null) {
            game.getGameState().sendMessage(_playerId + " reconciles");
            _effectQueue = new LinkedList<>();

            final int handSize = game.getFormat().getHandSize();

            GameState gameState = _game.getGameState();
            final Set<? extends PhysicalCard> cardsInHand = new HashSet<PhysicalCard>(gameState.getHand(_playerId));
            if (cardsInHand.size() > handSize) {
                _effectQueue.add(new PlayoutDecisionEffect(_playerId,
                        new CardsSelectionDecision(1, "Choose cards to discard down to "+handSize, cardsInHand, cardsInHand.size() - handSize, cardsInHand.size() - handSize) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                Set<PhysicalCard> cards = getSelectedCardsByResponse(result);
                                _effectQueue.add(new DiscardCardsFromHandEffect(null, _playerId, cards, false));
                                _effectQueue.add(
                                        new TriggeringResultEffect(new ReconcileResult(_playerId), "Player reconciled"));
                            }
                        }));
            } else if (cardsInHand.size() > 0) {
                _effectQueue.add(new PlayoutDecisionEffect(_playerId,
                        new CardsSelectionDecision(1, "Reconcile - choose card to discard or press DONE", cardsInHand, 0, 1) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                Set<PhysicalCard> selectedCards = getSelectedCardsByResponse(result);
                                if (selectedCards.size() > 0) {
                                    _effectQueue.add(new DiscardCardsFromHandEffect(null, _playerId, selectedCards, false));
                                }
                                int cardsInHandAfterDiscard = cardsInHand.size() - selectedCards.size();
                                if (cardsInHandAfterDiscard < handSize) {
                                    _effectQueue.add(new DrawCardsEffect(PlayerReconcilesAction.this, _playerId, handSize - cardsInHandAfterDiscard));
                                }
                                _effectQueue.add(
                                        new TriggeringResultEffect(new ReconcileResult(_playerId), "Player reconciled"));
                            }
                        }));
            } else {
                _effectQueue.add(new DrawCardsEffect(PlayerReconcilesAction.this, _playerId, handSize));
                _effectQueue.add(
                        new TriggeringResultEffect(new ReconcileResult(_playerId), "Player reconciled"));
            }
        }

        return _effectQueue.poll();
    }
}
