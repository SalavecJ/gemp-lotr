package com.gempukku.lotro.logic.decisions;

import com.alibaba.fastjson2.JSONObject;
import com.gempukku.lotro.game.PhysicalCard;

import java.util.*;

public abstract class ArbitraryCardsSelectionDecision extends AbstractAwaitingDecision {
    private final Collection<? extends PhysicalCard> _physicalCards;
    private final Collection<? extends PhysicalCard> _selectable;
    private final int _minimum;
    private final int _maximum;
    private final int _source;

    public ArbitraryCardsSelectionDecision(int id, String text, Collection<? extends PhysicalCard> physicalCards,
                                           int minimum, int maximum, int source) {
        this(id, text, physicalCards, physicalCards, minimum, maximum, source);
    }

    public ArbitraryCardsSelectionDecision(int id, String text, Collection<? extends PhysicalCard> physicalCards,
                                           Collection<? extends PhysicalCard> selectable,
                                           int minimum, int maximum, int source) {
        super(id, text, AwaitingDecisionType.ARBITRARY_CARDS);
        _selectable = selectable;
        _minimum = minimum;
        _maximum = maximum;
        _source = source;
        List<PhysicalCard> physicalCardsList = new ArrayList<>(physicalCards);
        physicalCardsList.sort(Comparator.comparingInt(PhysicalCard::getCardId));

        _physicalCards = physicalCardsList;

        setParam("min", String.valueOf(minimum));
        setParam("max", String.valueOf(maximum));
        setParam("cardId", getTempCardIds(physicalCardsList));
        setParam("physicalId", getPhysicalCardIds(physicalCardsList));
        setParam("blueprintId", getBlueprintIds(physicalCardsList));
        setParam("selectable", getSelectable(physicalCardsList, selectable));
        setParam("source", String.valueOf(source));
    }

    public ArbitraryCardsSelectionDecision(int id, String text, List<String> physicalCardIds, List<String> blueprintIds,
                                           List<String> selectable, int minimum, int maximum, int source) {
        super(id, text, AwaitingDecisionType.ARBITRARY_CARDS);
        _physicalCards = null;
        _selectable = null;
        _minimum = minimum;
        _maximum = maximum;
        _source = source;
        setParam("min", String.valueOf(minimum));
        setParam("max", String.valueOf(maximum));
        setParam("cardId", physicalCardIds.toArray(new String[0]));
        setParam("blueprintId", blueprintIds.toArray(new String[0]));
        setParam("selectable", selectable.toArray(new String[0]));
        setParam("source", String.valueOf(source));
    }

    private String[] getSelectable(Collection<? extends PhysicalCard> physicalCards, Collection<? extends PhysicalCard> selectable) {
        String[] result = new String[physicalCards.size()];
        int index = 0;
        for (PhysicalCard physicalCard : physicalCards) {
            result[index] = String.valueOf(selectable.contains(physicalCard));
            index++;
        }
        return result;
    }

    private String[] getTempCardIds(Collection<? extends PhysicalCard> physicalCards) {
        String[] result = new String[physicalCards.size()];
        for (int i = 0; i < physicalCards.size(); i++)
            result[i] = "temp" + i;
        return result;
    }

    private String[] getPhysicalCardIds(Collection<? extends PhysicalCard> physicalCards) {
        String[] result = new String[physicalCards.size()];
        int i = 0;
        for (PhysicalCard physicalCard : physicalCards) {
            result[i] = String.valueOf(physicalCard.getCardId());
            i++;
        }
        return result;
    }

    private String[] getBlueprintIds(Collection<? extends PhysicalCard> physicalCards) {
        String[] result = new String[physicalCards.size()];
        int index = 0;
        for (PhysicalCard physicalCard : physicalCards) {
            result[index] = physicalCard.getBlueprintId();
            index++;
        }
        return result;
    }

    protected PhysicalCard getPhysicalCardByIndex(int index) {
        int i = 0;
        for (PhysicalCard physicalCard : _physicalCards) {
            if (i == index)
                return physicalCard;
            i++;
        }
        return null;
    }

    protected List<PhysicalCard> getSelectedCardsByResponse(String response) throws DecisionResultInvalidException {
        String[] cardIds;
        if (response.isEmpty())
            cardIds = new String[0];
        else
            cardIds = response.split(",");

        if (cardIds.length < _minimum || cardIds.length > _maximum)
            throw new DecisionResultInvalidException();

        List<PhysicalCard> result = new LinkedList<>();
        try {
            for (String cardId : cardIds) {
                PhysicalCard card = getPhysicalCardByIndex(Integer.parseInt(cardId.substring(4)));
                if (result.contains(card) || !_selectable.contains(card))
                    throw new DecisionResultInvalidException();
                result.add(card);
            }
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            throw new DecisionResultInvalidException();
        }

        return result;
    }

    @Override
    public JSONObject toJson() {
        JSONObject obj = new JSONObject();
        obj.put("type", "ArbitraryCardsSelectionDecision");
        obj.put("id", getAwaitingDecisionId());
        obj.put("text", getText());
        obj.put("min", _minimum);
        obj.put("max", _maximum);
        obj.put("tempCards", getDecisionParameters().get("cardId"));
        obj.put("physicalCards", getDecisionParameters().get("physicalId"));
        obj.put("blueprintIds", getDecisionParameters().get("blueprintId"));
        obj.put("selectable", getDecisionParameters().get("selectable"));
        obj.put("source", _source);
        return obj;
    }

    public static ArbitraryCardsSelectionDecision fromJson(JSONObject obj) {
        return new ArbitraryCardsSelectionDecision(obj.getInteger("id"), obj.getString("text"), Arrays.asList(obj.getObject("tempCards", String[].class)),
                Arrays.asList(obj.getObject("blueprintIds", String[].class)), Arrays.asList(obj.getObject("selectable", String[].class)),
                obj.getInteger("min"), obj.getInteger("max"), obj.getInteger("source")) {
            @Override
            public void decisionMade(String result) throws DecisionResultInvalidException {
                throw new UnsupportedOperationException("Not implemented in training context");
            }
        };
    }
}

