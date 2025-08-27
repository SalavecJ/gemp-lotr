package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.timing.AbstractEffect;
import com.gempukku.lotro.logic.timing.Effect;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public abstract class ChooseArbitraryCardsEffect extends AbstractEffect {
    private final String _playerId;
    private final String _choiceText;
    private final boolean _showMatchingOnly;
    private final Collection<PhysicalCard> _cards;
    private final Filterable _filter;
    private final int _minimum;
    private final int _maximum;
    private final PhysicalCard _source;


    public ChooseArbitraryCardsEffect(String playerId, String choiceText, Collection<? extends PhysicalCard> cards,
                                      int minimum, int maximum, PhysicalCard source) {
        this(playerId, choiceText, cards, Filters.any, minimum, maximum, source);
    }

    public ChooseArbitraryCardsEffect(String playerId, String choiceText, Collection<? extends PhysicalCard> cards,
                                      Filterable filter, int minimum, int maximum, PhysicalCard source) {
        this(playerId, choiceText, cards, filter, minimum, maximum, false, source);
    }

    public ChooseArbitraryCardsEffect(String playerId, String choiceText, Collection<? extends PhysicalCard> cards,
                                      Filterable filter, int minimum, int maximum, boolean showMatchingOnly,
                                      PhysicalCard source) {
        _playerId = playerId;
        _choiceText = choiceText;
        _showMatchingOnly = showMatchingOnly;
        _cards = new HashSet<>(cards);
        _filter = filter;
        _minimum = minimum;
        _maximum = maximum;
        _source = source;
    }

    @Override
    public boolean isPlayableInFull(LotroGame game) {
        return Filters.filter(game, _cards, _filter).size() >= _minimum;
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
    protected FullEffectResult playEffectReturningResult(final LotroGame game) {
        Collection<PhysicalCard> possibleCards = Filters.filter(game, _cards, _filter);

        boolean success = possibleCards.size() >= _minimum;

        int minimum = _minimum;

        if (possibleCards.size() < minimum)
            minimum = possibleCards.size();

        if (_maximum == 0) {
            cardsSelected(game, Collections.emptySet());
        } else if (possibleCards.size() == minimum) {
            cardsSelected(game, possibleCards);
        } else {
            Collection<PhysicalCard> toShow = _cards;
            if (_showMatchingOnly)
                toShow = possibleCards;

            game.getUserFeedback().sendAwaitingDecision(_playerId,
                    new ArbitraryCardsSelectionDecision(1, _choiceText, toShow, possibleCards, _minimum, _maximum, _source != null ? _source.getCardId() : -1) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            cardsSelected(game, getSelectedCardsByResponse(result));
                        }
                    });
        }

        return new FullEffectResult(success);
    }

    public void removeCard(PhysicalCard card) {
        _cards.remove(card);
    }

    protected abstract void cardsSelected(LotroGame game, Collection<PhysicalCard> selectedCards);
}
