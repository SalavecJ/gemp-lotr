package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.timing.AbstractEffect;
import com.gempukku.lotro.logic.timing.Effect;

import java.util.Collections;

public class PutCardFromDeckOnBottomOfDeckEffect extends AbstractEffect {
    private final PhysicalCard _source;
    private final PhysicalCard _physicalCard;

    public PutCardFromDeckOnBottomOfDeckEffect(PhysicalCard source, PhysicalCard physicalCard) {
        _physicalCard = physicalCard;
        _source = source;
    }

    @Override
    public boolean isPlayableInFull(LotroGame game) {
        return _physicalCard.getZone() == Zone.DECK;
    }

    @Override
    public String getText(LotroGame game) {
        return "Put " + GameUtils.getFullName(_physicalCard) + " on the bottom of draw deck";
    }

    @Override
    public Effect.Type getType() {
        return null;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(LotroGame game) {
        if (isPlayableInFull(game)) {
            GameState gameState = game.getGameState();
            gameState.sendMessage(_physicalCard.getOwner() + " puts " + GameUtils.getCardLink(_physicalCard) + " from deck on the bottom of deck");
            gameState.removeCardsFromZone(_source.getOwner(), Collections.singleton(_physicalCard));
            gameState.putCardOnBottomOfDeck(_physicalCard);
            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }
}
