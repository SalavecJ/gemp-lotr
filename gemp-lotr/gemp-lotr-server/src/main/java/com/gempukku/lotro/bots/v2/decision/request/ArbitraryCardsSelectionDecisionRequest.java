package com.gempukku.lotro.bots.v2.decision.request;

import com.gempukku.lotro.bots.v2.decision.option.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ArbitraryCardsSelectionDecisionRequest extends DecisionRequest {
    private static final String STARTING_FELLOWSHIP_TEXT = "Starting fellowship - Choose next character or press DONE";

    private int min;
    private int max;

    private final List<Option> options = new ArrayList<>();

    public ArbitraryCardsSelectionDecisionRequest(ArbitraryCardsSelectionDecision decision, DefaultLotroGame game) throws CardNotFoundException {
        super(decision.getText());

        Map<String, String[]> params = decision.getDecisionParameters();
        String[] cardIds = params.get("cardId");
        String[] selectable = params.get("selectable");
        min = params.containsKey("min") ? Integer.parseInt(params.get("min")[0]) : 0;
        max = params.containsKey("max") ? Integer.parseInt(params.get("max")[0]) : cardIds.length;

        if (min == 0) {
            min = 1; // Explicit pass option added
        }

        if (cardIds == null || selectable == null || cardIds.length != selectable.length) {
            throw new IllegalArgumentException("Bad format of parameters for ArbitraryCardsSelectionDecision: " + decision.toJson().toString());
        }

        // Collect all selectable indices
        List<Integer> selectableIndices = new ArrayList<>();
        for (int i = 0; i < selectable.length; i++) {
            if (Boolean.parseBoolean(selectable[i])) {
                selectableIndices.add(i);
            }
        }

        // If nothing is selectable or max == 0, we have no options
        if (selectableIndices.isEmpty() || max == 0) {
            options.add(new PassOption());
            max = 1; // Explicit pass option
            return;
        }

        if (isChooseStartingFellowshipDecision(decision)) {
            createChooseStartingFellowshipOptions(decision);
        } else if (isChooseTargetForCardDecision(decision)) {
            createChooseTargetForCardOptions(decision, game);
        } else {
            throw new UnsupportedOperationException("Arbitrary cards selection decision request not supported: " + decision.toJson().toString());
        }
    }

    private boolean isChooseTargetForCardDecision(ArbitraryCardsSelectionDecision decision) {
        Map<String, String[]> params = decision.getDecisionParameters();
        String source = params.get("source")[0];

        return !source.equals("-1");
    }

    private void createChooseTargetForCardOptions(ArbitraryCardsSelectionDecision decision, DefaultLotroGame game) throws CardNotFoundException {
        Map<String, String[]> params = decision.getDecisionParameters();
        String[] tempCards = params.get("cardId");
        String[] blueprintIds = params.get("blueprintId");
        PhysicalCard sourceCard = game.getGameState().getPhysicalCard(Integer.parseInt(params.get("source")[0]));

        for (int i = 0; i < tempCards.length; i++) {
            int finalI = i;
            if (options.stream().anyMatch(option -> option instanceof ChooseArbitraryTargetForCardOption chooseOption && chooseOption.getBlueprint().getId().equals(blueprintIds[finalI]))) {
                continue; // Skip duplicates
            }
            options.add(new ChooseArbitraryTargetForCardOption(sourceCard, blueprintIds[i], tempCards[i]));
        }
    }

    private boolean isChooseStartingFellowshipDecision(ArbitraryCardsSelectionDecision decision) {
        return decision.getText().equals(STARTING_FELLOWSHIP_TEXT);
    }

    private void createChooseStartingFellowshipOptions(ArbitraryCardsSelectionDecision decision) throws CardNotFoundException {
        Map<String, String[]> params = decision.getDecisionParameters();
        String[] tempCards = params.get("cardId");
        String[] blueprintIds = params.get("blueprintId");

        for (int i = 0; i < tempCards.length; i++) {
            int finalI = i;
            if (options.stream().anyMatch(option -> option instanceof StartingCompanionOption chooseOption && chooseOption.getBlueprint().getId().equals(blueprintIds[finalI]))) {
                continue; // Skip duplicates
            }
            options.add(new StartingCompanionOption(blueprintIds[i], tempCards[i]));
        }

        options.add(new PassOption());
    }

    @Override
    public List<Option> getOptions() {
        return options;
    }

    @Override
    public int getMinToSelect() {
        return min;
    }

    @Override
    public int getMaxToSelect() {
        return max;
    }
}
