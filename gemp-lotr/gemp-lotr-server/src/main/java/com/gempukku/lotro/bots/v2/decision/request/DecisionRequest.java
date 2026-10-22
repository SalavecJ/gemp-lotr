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

}
