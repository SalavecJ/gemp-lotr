package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.actions.SubAction;
import com.gempukku.lotro.logic.timing.AbstractSubActionEffect;
import com.gempukku.lotro.logic.timing.Action;

import java.util.Collection;

public class PutCardsFromHandBeneathDrawDeckEffect extends AbstractSubActionEffect {
    private final Action _action;
    private final String _playerId;
    private final Filterable[] _filters;

    public PutCardsFromHandBeneathDrawDeckEffect(Action action, String playerId, Filterable... filters) {
        _action = action;
        _playerId = playerId;
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
        return true;
    }

    @Override
    public void playEffect(LotroGame game) {
        final Collection<PhysicalCard> cards = Filters.filter(game.getGameState().getHand(_playerId), game, _filters);
        SubAction subAction = new SubAction(_action);
        subAction.appendEffect(
                new ChooseAndPutNextCardFromHandOnBottomOfLibrary(subAction, cards));
        processSubAction(game, subAction);
    }

    private class ChooseAndPutNextCardFromHandOnBottomOfLibrary extends ChooseArbitraryCardsEffect {
        private final Collection<PhysicalCard> _remainingCards;
        private final CostToEffectAction _subAction;

        public ChooseAndPutNextCardFromHandOnBottomOfLibrary(CostToEffectAction subAction, Collection<PhysicalCard> remainingCards) {
            super(_playerId, "Choose a card to put on bottom of your deck", remainingCards, 1, 1);
            _subAction = subAction;
            _remainingCards = remainingCards;
        }

        @Override
        protected void cardsSelected(LotroGame game, Collection<PhysicalCard> selectedCards) {
            for (PhysicalCard selectedCard : selectedCards) {
                _subAction.appendEffect(
                        new PutCardFromHandOnBottomOfDeckEffect(selectedCard));
                _remainingCards.remove(selectedCard);
                if (_remainingCards.size() > 0)
                    _subAction.appendEffect(
                            new ChooseAndPutNextCardFromHandOnBottomOfLibrary(_subAction, _remainingCards));
            }
        }
    }
}
