package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.timing.AbstractEffect;
import com.gempukku.lotro.logic.timing.Effect;

public class DiscardBottomCardFromDeckEffect extends AbstractEffect {
    private final String _playerId;

    public DiscardBottomCardFromDeckEffect(String playerId) {
        _playerId = playerId;
    }

    @Override
    public String getText(LotroGame game) {
        return null;
    }

    @Override
    public Effect.Type getType() {
        return null;
    }

    @Override
    public boolean isPlayableInFull(LotroGame game) {
        return game.getGameState().getDeck(_playerId).size() > 0;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(LotroGame game) {
        boolean success = game.getGameState().getDeck(_playerId).size() > 0;
        if (success) {
            GameState gameState = game.getGameState();
            PhysicalCard card = gameState.removeBottomDeckCard(_playerId);
            gameState.sendMessage(_playerId + " discards bottom card from their deck - " + GameUtils.getCardLink(card));
            gameState.addCardToZone(game, card, Zone.DISCARD);

            discardedCardCallback(card);

            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }

    protected void discardedCardCallback(PhysicalCard card) {

    }
}
