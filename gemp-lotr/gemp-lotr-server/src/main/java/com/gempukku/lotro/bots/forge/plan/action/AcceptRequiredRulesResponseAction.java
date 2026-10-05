package com.gempukku.lotro.bots.forge.plan.action;

public class AcceptRequiredRulesResponseAction extends ChooseCardAction {
    private final String answerText;

    public AcceptRequiredRulesResponseAction(String decisionText, String answerText, String actionId) {
        super(decisionText, null, actionId);
        this.answerText = answerText;
    }

    @Override
    public String toString() {
        return "Action: Accept required rules response - " + answerText;
    }
}
