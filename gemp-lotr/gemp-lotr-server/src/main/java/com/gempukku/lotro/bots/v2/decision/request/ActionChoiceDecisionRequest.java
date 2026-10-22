package com.gempukku.lotro.bots.v2.decision.request;

import com.gempukku.lotro.bots.v2.decision.option.Option;
import com.gempukku.lotro.bots.v2.decision.option.RequiredResponseOption;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.decisions.ActionSelectionDecision;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ActionChoiceDecisionRequest extends DecisionRequest {
    private static final String REQUIRED_RESPONSES_TEXT = "Required responses";

    private final List<Option> options = new ArrayList<>();

    public ActionChoiceDecisionRequest(ActionSelectionDecision decision, DefaultLotroGame game) {
        super(decision.getText());

        Map<String, String[]> params = decision.getDecisionParameters();
        String[] actionIds = params.get("actionId");
        if (actionIds == null || actionIds.length == 0) {
            throw new IllegalArgumentException("No actions provided for required choice");
        }

        if (isRequiredResponsesDecision(decision)) {
            createRequiredResponsesOptions(decision, game);
        } else {
            throw new UnsupportedOperationException("Action choice decision request not supported: " + decision.toJson().toString());
        }
    }

    private boolean isRequiredResponsesDecision(ActionSelectionDecision decision) {
        return decision.getText().equals(REQUIRED_RESPONSES_TEXT);
    }

    private void createRequiredResponsesOptions(ActionSelectionDecision decision, DefaultLotroGame game) {
        Map<String, String[]> params = decision.getDecisionParameters();
        String[] sourceIds = params.get("sourceId");
        String[] actionTexts = params.get("actionText");

        for (int i = 0; i < actionTexts.length; i++) {
            String sourceId = sourceIds[i];
            PhysicalCard sourceCard = sourceId.equals("rules") ? null : game.getGameState().getPhysicalCard(Integer.parseInt(sourceIds[i]));
            options.add(new RequiredResponseOption(sourceCard, actionTexts[i], i));
        }
    }

    @Override
    public List<Option> getOptions() {
        return options;
    }
}
