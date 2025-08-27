package com.gempukku.lotro.logic.decisions;

import com.alibaba.fastjson2.JSONObject;

public abstract class IntegerAwaitingDecision extends AbstractAwaitingDecision {
    private final Integer _min;
    private final Integer _max;
    private final int _source;

    public IntegerAwaitingDecision(int id, String text, Integer min, int source) {
        this(id, text, min, null, source);
    }

    public IntegerAwaitingDecision(int id, String text, Integer min, Integer max, int source) {
        this(id, text, min, max, null, source);
    }

    public IntegerAwaitingDecision(int id, String text, Integer min, Integer max, Integer defaultValue, int source) {
        super(id, text, AwaitingDecisionType.INTEGER);
        _min = min;
        _max = max;
        if (min != null)
            setParam("min", min.toString());
        if (max != null)
            setParam("max", max.toString());
        if (defaultValue != null)
            setParam("defaultValue", defaultValue.toString());
        _source = source;
        setParam("source", String.valueOf(source));
    }

    protected int getValidatedResult(String result) throws DecisionResultInvalidException {
        try {
            int value = Integer.parseInt(result);
            if (_min != null && _min > value)
                throw new DecisionResultInvalidException();
            if (_max != null && _max < value)
                throw new DecisionResultInvalidException();

            return value;
        } catch (NumberFormatException exp) {
            throw new DecisionResultInvalidException();
        }
    }

    @Override
    public JSONObject toJson() {
        JSONObject obj = new JSONObject();
        obj.put("type", "IntegerAwaitingDecision");
        obj.put("id", getAwaitingDecisionId());
        obj.put("text", getText());
        if (_min != null)
            obj.put("min", _min);
        if (_max != null)
            obj.put("max", _max);
        obj.put("source", _source);
        return obj;
    }

    public static IntegerAwaitingDecision fromJson(JSONObject obj) {
        return new IntegerAwaitingDecision(obj.getInteger("id"), obj.getString("text"), obj.getInteger("min"), obj.getInteger("max"), obj.getInteger("source")) {
            @Override
            public void decisionMade(String result) throws DecisionResultInvalidException {
                throw new UnsupportedOperationException("Not implemented in training context");
            }
        };
    }
}
