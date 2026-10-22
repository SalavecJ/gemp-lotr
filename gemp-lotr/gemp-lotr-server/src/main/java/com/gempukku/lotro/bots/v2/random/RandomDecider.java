package com.gempukku.lotro.bots.v2.random;

import com.gempukku.lotro.bots.v2.decision.Decider;
import com.gempukku.lotro.bots.v2.decision.option.AssignMinionOption;
import com.gempukku.lotro.bots.v2.decision.option.Option;
import com.gempukku.lotro.bots.v2.decision.request.AssignMinionsDecisionRequest;
import com.gempukku.lotro.bots.v2.decision.request.DecisionRequest;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.ArrayList;
import java.util.List;

public class RandomDecider implements Decider {

    @Override
    public List<Option> decide(DecisionRequest decisionRequest, DefaultLotroGame game) {
        int minToSelect = decisionRequest.getMinToSelect();
        int maxToSelect = decisionRequest.getMaxToSelect();

        if (minToSelect > maxToSelect) {
            throw new IllegalArgumentException("minToSelect cannot be greater than maxToSelect");
        } else if (maxToSelect <= 0) {
            throw new IllegalArgumentException("maxToSelect must be greater than 0");
        } else if (minToSelect <= 0) {
            throw new IllegalArgumentException("minToSelect must be greater than 0");
        } else if (decisionRequest.getOptions().size() < minToSelect) {
            throw new IllegalArgumentException("Not enough options to select the minimum required");
        }

        if (decisionRequest instanceof AssignMinionsDecisionRequest assignMinionsDecisionRequest) {
            return randomlyDecideAssignment(assignMinionsDecisionRequest);
        }

        if (minToSelect == 1 && maxToSelect == 1) {
            int randomIndex = (int) (Math.random() * decisionRequest.getOptions().size());
            return List.of(decisionRequest.getOptions().get(randomIndex));
        } else {
            int realMaxToSelect = Math.min(maxToSelect, decisionRequest.getOptions().size());
            int optionsToSelect = minToSelect + (int) (Math.random() * (realMaxToSelect - minToSelect + 1));

            List<Integer> selectedIndices = new ArrayList<>();
            while (selectedIndices.size() < optionsToSelect) {
                int randomIndex = (int) (Math.random() * decisionRequest.getOptions().size());
                if (!selectedIndices.contains(randomIndex)) {
                    selectedIndices.add(randomIndex);
                }
            }

            List<Option> selectedOptions = new ArrayList<>();
            for (int index : selectedIndices) {
                selectedOptions.add(decisionRequest.getOptions().get(index));
            }

            return selectedOptions;
        }
    }

    private List<Option> randomlyDecideAssignment(AssignMinionsDecisionRequest assignMinionsDecisionRequest) {
        while (assignMinionsDecisionRequest.getNextMinion() != null) {
            int randomIndex = (int) (Math.random() * assignMinionsDecisionRequest.getOptions().size());
            AssignMinionOption selectedOption = ((AssignMinionOption) assignMinionsDecisionRequest.getOptions().get(randomIndex));
            assignMinionsDecisionRequest.assign(selectedOption);
        }
        return assignMinionsDecisionRequest.getChosenOptions();
    }
}
