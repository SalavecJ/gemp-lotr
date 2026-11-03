package com.gempukku.lotro.bots.v2.decision.request;

import com.gempukku.lotro.bots.v2.decision.option.*;

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

    public boolean isBurdenBidDecision() {
        return getOptions().stream().allMatch(option -> option instanceof BurdenBidOption);
    }

    public boolean isStartingPositionDecision() {
        return getOptions().stream().allMatch(option -> option instanceof StartingPositionOption);
    }

    public boolean isStartingCompanionDecision() {
        return getOptions().stream().anyMatch(option -> option instanceof StartingCompanionOption);
    }

    public boolean isMulliganDecision() {
        return getOptions().stream().allMatch(option -> option instanceof MulliganOption);
    }

    public boolean isSanctuaryHealDecision() {
        return getOptions().stream().anyMatch(option -> option instanceof SanctuaryHealOption);
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
