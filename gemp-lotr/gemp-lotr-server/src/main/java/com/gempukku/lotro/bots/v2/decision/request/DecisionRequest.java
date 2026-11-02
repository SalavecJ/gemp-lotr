package com.gempukku.lotro.bots.v2.decision.request;

import com.gempukku.lotro.bots.v2.decision.option.Option;

import java.util.List;

public abstract class DecisionRequest {

    private final String text;

    public DecisionRequest(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public abstract List<Option> getOptions();

    public int getMinToSelect() {
        return 1;
    }

    public int getMaxToSelect() {
        return 1;
    }

    public String combineSelected(List<Option> picked) {
        if (getMaxToSelect() == 1) {
            throw new UnsupportedOperationException("This decision does not support multiple selections");
        } else {
            return String.join(",", picked.stream().map(Option::getAnswerForGame).toList());
        }
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(text);

        for (int i = 0; i < getOptions().size(); i++) {
            result.append("\n");
            result.append(i + 1).append(": ").append(getOptions().get(i));
        }

        return result.toString();
    }
}
