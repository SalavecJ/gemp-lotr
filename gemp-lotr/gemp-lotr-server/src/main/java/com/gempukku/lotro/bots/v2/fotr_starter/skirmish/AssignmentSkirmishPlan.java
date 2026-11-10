package com.gempukku.lotro.bots.v2.fotr_starter.skirmish;

import com.gempukku.lotro.bots.v2.decision.option.AssignMinionOption;
import com.gempukku.lotro.bots.v2.decision.option.Option;
import com.gempukku.lotro.bots.v2.decision.request.AssignMinionsDecisionRequest;
import com.gempukku.lotro.bots.v2.decision.request.DecisionRequest;
import com.gempukku.lotro.bots.v2.fotr_starter.Plan;

import java.util.List;

public class AssignmentSkirmishPlan implements Plan {
    private int actionsTaken = 0;
    private final List<List<Option>> actionsToTake;

    public AssignmentSkirmishPlan(List<List<Option>> actionsToTake) {
        this.actionsToTake = actionsToTake;
    }

    @Override
    public List<Option> nextAction(DecisionRequest decisionRequest) {
        List<Option> tbr = actionsToTake.get(actionsTaken);

        if (tbr.stream().allMatch(option -> option instanceof AssignMinionOption)) {
            return decideAssignmentBasedOnPlan((AssignMinionsDecisionRequest) decisionRequest);
        }

        if (tbr.stream().anyMatch(option -> !decisionRequest.getOptions().contains(option))) {
            throw new IllegalStateException("Next action is not available in the current decision request");
        }
        return actionsToTake.get(actionsTaken++);
    }

    @Override
    public List<List<Option>> getAllOptions() {
        return actionsToTake;
    }

    @Override
    public boolean isComplete() {
        return actionsTaken >= actionsToTake.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < actionsToTake.size(); i++) {
            sb.append(i + 1).append(". ");
            sb.append(actionsToTake.get(i).stream().map(Option::toString).reduce((s1, s2) -> s1 + "; " + s2).orElse(""));
            if (i < actionsToTake.size() - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }


    private List<Option> decideAssignmentBasedOnPlan(AssignMinionsDecisionRequest assignMinionsDecisionRequest) {
        for (Option plannedOption : actionsToTake.get(actionsTaken)) {
            List<Option> availableOptions = assignMinionsDecisionRequest.getOptions();
            Option matchingOption = availableOptions.stream()
                    .filter(option -> option.equals(plannedOption))
                    .findFirst()
                    .orElse(null);
            if (matchingOption == null) {
                throw new IllegalStateException("Planned option " + plannedOption + " is not available in the current assignment decision. Available options: " + availableOptions);
            }
            assignMinionsDecisionRequest.assign((AssignMinionOption) matchingOption);
        }
        actionsTaken++;
        return assignMinionsDecisionRequest.getChosenOptions();
    }
}
