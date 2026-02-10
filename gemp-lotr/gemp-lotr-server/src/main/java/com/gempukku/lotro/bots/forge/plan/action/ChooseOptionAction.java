package com.gempukku.lotro.bots.forge.plan.action;

import com.gempukku.lotro.logic.decisions.AwaitingDecision;

public class ChooseOptionAction extends ActionToTake {
    private final AwaitingDecision decision;
    private final String chosenOption;

    public ChooseOptionAction(AwaitingDecision decision, String chosenOption) {
        super(decision.getText());
        this.decision = decision;
        this.chosenOption = chosenOption;
    }

    @Override
    public String carryOut() {
        return getWantedOptionIndex(decision, chosenOption);
    }

    @Override
    public String toString() {
        return "Action: Choose '" + chosenOption + "'";
    }

    private String getWantedOptionIndex(AwaitingDecision awaitingDecision, String wantedOption) {
        String[] options = awaitingDecision.getDecisionParameters().get("results");
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(wantedOption)) {
                return String.valueOf(i);
            }
        }
        throw new UnsupportedOperationException("Decision not supported: " + awaitingDecision.toJson().toString());
    }
}
