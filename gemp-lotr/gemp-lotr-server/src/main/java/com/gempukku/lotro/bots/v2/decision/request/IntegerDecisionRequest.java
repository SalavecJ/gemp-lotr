package com.gempukku.lotro.bots.v2.decision.request;

import com.gempukku.lotro.bots.v2.decision.option.BurdenBidOption;
import com.gempukku.lotro.bots.v2.decision.option.ChooseNumberForCardOption;
import com.gempukku.lotro.bots.v2.decision.option.Option;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.decisions.IntegerAwaitingDecision;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IntegerDecisionRequest extends DecisionRequest {
    private static final String BURDEN_BID_TEXT = "Choose a number of burdens to bid";

    private int min = 0;
    private int max = 9;

    private final List<Option> options = new ArrayList<>();

    public IntegerDecisionRequest(IntegerAwaitingDecision decision, DefaultLotroGame game) {
        super(decision.getText());

        Map<String, String[]> params = decision.getDecisionParameters();
        if (params.containsKey("min")) {
            min = Integer.parseInt(params.get("min")[0]);
        }
        if (params.containsKey("max")) {
            max = Integer.parseInt(params.get("max")[0]);
        }

        if (isBurdenBidDecision(decision)) {
            createBurdenBidOptions();
        } else if (isChooseNumberForCardDecision(decision, game)) {
            createChooseNumberForCardOptions(decision, game);
        } else {
            throw new UnsupportedOperationException("Integer decision request not supported: " + decision.toJson().toString());
        }
    }

    private boolean isChooseNumberForCardDecision(IntegerAwaitingDecision decision, DefaultLotroGame game) {
        return !decision.getDecisionParameters().get("source")[0].equals("-1"); //not triggered by rules
    }

    private void createChooseNumberForCardOptions(IntegerAwaitingDecision decision, DefaultLotroGame game) {
        int sourceCardId = Integer.parseInt(decision.getDecisionParameters().get("source")[0]);
        PhysicalCard sourceCard = game.getGameState().getPhysicalCard(sourceCardId);
        int minValue = Integer.parseInt(decision.getDecisionParameters().get("min")[0]);
        int maxValue = Integer.parseInt(decision.getDecisionParameters().get("max")[0]);

        for (int i = minValue; i <= maxValue; i++) {
            options.add(new ChooseNumberForCardOption(sourceCard, i));
        }
    }

    private boolean isBurdenBidDecision(IntegerAwaitingDecision decision) {
        return decision.getText().equals(BURDEN_BID_TEXT);
    }

    private void createBurdenBidOptions() {
        for (int i = min; i <= max; i++) {
            options.add(new BurdenBidOption(i));
        }
    }

    @Override
    public List<Option> getOptions() {
        return options;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }
}
