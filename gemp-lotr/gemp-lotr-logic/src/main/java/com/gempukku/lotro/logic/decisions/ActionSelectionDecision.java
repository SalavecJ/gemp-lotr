package com.gempukku.lotro.logic.decisions;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.timing.Action;

import java.util.Comparator;
import java.util.List;

public abstract class ActionSelectionDecision extends AbstractAwaitingDecision {
    private static final String RULES_SOURCE = "rules";

    private final LotroGame _game;
    private final List<Action> _actions;

    private record SortableAction(Action action, PhysicalCard source, String text) {
    }

    public ActionSelectionDecision(LotroGame game, int decisionId, String text, List<? extends Action> actions) {
        super(decisionId, text, AwaitingDecisionType.ACTION_CHOICE);
        _game = game;

        // rule actions first (ordered by text), then card actions ordered by card id and text
        List<SortableAction> sortedActions = actions.stream()
                .map(action -> new SortableAction(action, action.getActionAttachedToCard(), action.getText(_game)))
                .sorted(Comparator
                        .comparing((SortableAction sortable) -> sortable.source() != null)
                        .thenComparingInt(sortable -> sortable.source() != null ? sortable.source().getCardId() : 0)
                        .thenComparing(SortableAction::text, Comparator.nullsFirst(Comparator.naturalOrder())))
                .toList();
        _actions = sortedActions.stream().map(SortableAction::action).toList();

        setParam("actionId", getActionIds(sortedActions));
        setParam("sourceId", getSourceIds(sortedActions));
        setParam("blueprintId", getBlueprintIds(sortedActions));
        setParam("actionText", getActionTexts(sortedActions));
    }

    private String[] getActionIds(List<SortableAction> actions) {
        String[] result = new String[actions.size()];
        for (int i = 0; i < result.length; i++)
            result[i] = String.valueOf(i);
        return result;
    }

    private String[] getSourceIds(List<SortableAction> actions) {
        String[] result = new String[actions.size()];
        for (int i = 0; i < result.length; i++) {
            PhysicalCard physicalCard = actions.get(i).source();
            if (physicalCard != null)
                result[i] = String.valueOf(physicalCard.getCardId());
            else
                result[i] = RULES_SOURCE;
        }
        return result;
    }

    private String[] getBlueprintIds(List<SortableAction> actions) {
        String[] result = new String[actions.size()];
        for (int i = 0; i < result.length; i++) {
            PhysicalCard physicalCard = actions.get(i).source();
            if (physicalCard != null)
                result[i] = String.valueOf(physicalCard.getBlueprintId());
            else
                result[i] = RULES_SOURCE;
        }
        return result;
    }

    private String[] getActionTexts(List<SortableAction> actions) {
        String[] result = new String[actions.size()];
        for (int i = 0; i < result.length; i++)
            result[i] = actions.get(i).text();
        return result;
    }
    protected Action getSelectedAction(String result) throws DecisionResultInvalidException {
        if (result.isEmpty())
            throw new DecisionResultInvalidException();

        try {
            int actionIndex = Integer.parseInt(result);
            if (actionIndex < 0 || actionIndex >= _actions.size())
                throw new DecisionResultInvalidException();

            return _actions.get(actionIndex);
        } catch (NumberFormatException exp) {
            throw new DecisionResultInvalidException();
        }
    }
}
