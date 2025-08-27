package com.gempukku.lotro.logic.decisions;

public class YesNoDecision extends MultipleChoiceAwaitingDecision {
    public YesNoDecision(String text, int source) {
        super(1, text, new String[]{"Yes", "No"}, source);
    }

    @Override
    protected final void validDecisionMade(int index, String result) {
        if (index == 0)
            yes();
        else
            no();
    }

    protected void yes() {

    }

    protected void no() {

    }
}
