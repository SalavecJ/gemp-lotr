package com.gempukku.lotro.logic.effects.choose;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.actions.SubAction;
import com.gempukku.lotro.logic.effects.ChooseArbitraryCardsEffect;
import com.gempukku.lotro.logic.effects.PutCardFromHandOnBottomOfDeckEffect;
import com.gempukku.lotro.logic.timing.AbstractSubActionEffect;
import com.gempukku.lotro.logic.timing.Action;

import java.util.Collection;

public class ChooseAndPutCardsFromHandBeneathDrawDeckEffect extends AbstractSubActionEffect {
    private final Action _action;
    private final String _playerId;
    private final int _count;
    private final Filterable[] _filters;

    public ChooseAndPutCardsFromHandBeneathDrawDeckEffect(Action action, String playerId, int count, Filterable... filters) {
        _action = action;
        _playerId = playerId;
        _count = count;
        _filters = filters;
    }

    @Override
    public String getText(LotroGame game) {
        return null;
    }

    @Override
    public Type getType() {
        return null;
    }

    @Override
    public boolean isPlayableInFull(LotroGame game) {
        return Filters.filter(game.getGameState().getHand(_playerId), game, _filters).size() >= _count;
    }

    @Override
    public void playEffect(LotroGame game) {
        final Collection<PhysicalCard> cards = Filters.filter(game.getGameState().getHand(_playerId), game, _filters);
        SubAction subAction = new SubAction(_action);
        subAction.appendEffect(
                new ChooseAndPutNextCardFromHandOnBottomOfLibrary(subAction, Math.min(_count, cards.size()), cards));
        processSubAction(game, subAction);
    }

    private class ChooseAndPutNextCardFromHandOnBottomOfLibrary extends ChooseArbitraryCardsEffect {
        private final Collection<PhysicalCard> _remainingCards;
        private final CostToEffectAction _subAction;
        private final int _remainingCount;

        public ChooseAndPutNextCardFromHandOnBottomOfLibrary(CostToEffectAction subAction, int remainingCount, Collection<PhysicalCard> remainingCards) {
            super(_playerId, "Choose a card to put on bottom of your deck", remainingCards, 1, 1);
            _subAction = subAction;
            _remainingCount = remainingCount;
            _remainingCards = remainingCards;
        }

        @Override
        protected void cardsSelected(LotroGame game, Collection<PhysicalCard> selectedCards) {
            for (PhysicalCard selectedCard : selectedCards) {
                _subAction.appendEffect(
                        new PutCardFromHandOnBottomOfDeckEffect(selectedCard));
                _remainingCards.remove(selectedCard);
                if (_remainingCount - 1 > 0)
                    _subAction.appendEffect(
                            new ChooseAndPutNextCardFromHandOnBottomOfLibrary(_subAction, _remainingCount - 1, _remainingCards));
            }
        }
    }
}
