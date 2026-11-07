package com.gempukku.lotro.bots.v2.decision.request;

import com.gempukku.lotro.bots.v2.decision.option.*;
import com.gempukku.lotro.game.PhysicalCard;

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

    public boolean isFellowshipPhaseDecision() {
        return getText().equals("Play Fellowship action or Pass");
    }

    public boolean isShadowPhaseDecision() {
        return getText().equals("Play Shadow action or Pass");
    }

    public boolean isManeuverPhaseDecision() {
        return getText().equals("Play Maneuver action or Pass");
    }

    public boolean isArcheryPhaseDecision() {
        return getText().equals("Play Archery action or Pass");
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

    public boolean isHandReveal() {
        return getText().startsWith("Hand of ") && getOptions().size() == 1 && getOptions().getFirst() instanceof PassOption;
    }

    public boolean isChooseTargetForCardDecision() {
        return getOptions().stream().allMatch(option -> option instanceof ChooseTargetForCardOption);
    }

    public boolean isChooseArbitraryTargetForCardDecision() {
        return getOptions().stream().allMatch(option -> option instanceof ChooseArbitraryTargetForCardOption);
    }

    public boolean isChooseOptionForCardDecision() {
        return getOptions().stream().allMatch(option -> option instanceof ChooseOptionForCardOption);
    }

    public boolean isOptionalResponsesDecision() {
        return this instanceof CardActionSelectionDecisionRequest
                && (getText().equals(CardActionSelectionDecisionRequest.OPTIONAL_TRIGGER_RESPONSES_TEXT) || getText().endsWith(CardActionSelectionDecisionRequest.RESPONSES_KEYWORD_SUFFIX));
    }

    public PhysicalCard getSource() {
        if (isChooseTargetForCardDecision()) {
            return ((ChooseTargetForCardOption) getOptions().getFirst()).getSource();
        } else if (isChooseOptionForCardDecision()) {
            return ((ChooseOptionForCardOption) getOptions().getFirst()).getSource();
        } else if (isChooseArbitraryTargetForCardDecision()) {
            return ((ChooseArbitraryTargetForCardOption) getOptions().getFirst()).getSource();
        } else {
            throw new UnsupportedOperationException("This decision is not a choose target for card decision");
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
