package com.gempukku.lotro.logic.effects.choose;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.timing.AbstractEffect;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

public abstract class ChooseCardsFromDeadPileEffect extends AbstractEffect {
    private final String _playerId;
    private final int _minimum;
    private final int _maximum;
    private final Filter _filter;

    public ChooseCardsFromDeadPileEffect(String playerId, int minimum, int maximum, Filterable... filters) {
        _playerId = playerId;
        _minimum = minimum;
        _maximum = maximum;
        _filter = Filters.and(filters);
    }

    @Override
    public String getText(LotroGame game) {
        return "Choose card from dead pile";
    }

    @Override
    public Type getType() {
        return null;
    }

    @Override
    public boolean isPlayableInFull(LotroGame game) {
        Collection<PhysicalCard> cards = Filters.filter(game, game.getGameState().getDeadPile(game.getGameState().getCurrentPlayerId()), _filter);
        return cards.size() >= _minimum;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(final LotroGame game) {
        Collection<PhysicalCard> cards = Filters.filter(game, game.getGameState().getDeadPile(game.getGameState().getCurrentPlayerId()), _filter);

        boolean success = cards.size() >= _minimum;

        int minimum = Math.min(_minimum, cards.size());

        if (_maximum == 0) {
            cardsSelected(game, Collections.emptySet());
        } else if (cards.size() == minimum) {
            cardsSelected(game, cards);
        } else {
            game.getUserFeedback().sendAwaitingDecision(_playerId,
                    new ArbitraryCardsSelectionDecision(1, "Choose card from dead pile", new LinkedList<>(cards), minimum, _maximum) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            cardsSelected(game, getSelectedCardsByResponse(result));
                        }
                    });
        }

        return new FullEffectResult(success);
    }

    protected abstract void cardsSelected(LotroGame game, Collection<PhysicalCard> cards);
}
