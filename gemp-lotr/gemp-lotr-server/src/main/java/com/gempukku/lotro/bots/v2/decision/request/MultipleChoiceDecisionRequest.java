package com.gempukku.lotro.bots.v2.decision.request;

import com.gempukku.lotro.bots.v2.decision.option.*;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MultipleChoiceDecisionRequest extends DecisionRequest {
    private static final String CHOOSE_STARTING_POSITION_TEXT = "Choose one";
    private static final String MULLIGAN_TEXT = "Do you wish to mulligan? (Shuffle cards back and draw 6)";
    private static final String MOVE_AGAIN_TEXT = "Do you want to make another move?";

    private final List<Option> options = new ArrayList<>();

    public MultipleChoiceDecisionRequest(MultipleChoiceAwaitingDecision decision, DefaultLotroGame game) {
        super(decision.getText());

        Map<String, String[]> params = decision.getDecisionParameters();
        String[] legalOptions = params.get("results");
        if (legalOptions == null || legalOptions.length == 0) {
            throw new IllegalArgumentException("No options provided for multiple choice");
        }

        if (isChooseStartingPositionDecision(decision)) {
            createChooseStartingPositionOptions(decision);
        } else if (isMulliganDecision(decision)) {
            createMulliganOptions(decision);
        } else if (isMoveAgainDecision(decision)) {
            createMoveAgainOptions(decision);
        } else if (isChooseOptionForCardDecision(decision, game)) {
            createChooseOptionForCardOptions(decision, game);
        } else {
            throw new UnsupportedOperationException("Multiple choice decision request not supported: " + decision.toJson().toString());
        }
    }

    private boolean isChooseOptionForCardDecision(MultipleChoiceAwaitingDecision decision, DefaultLotroGame game) {
        return !decision.getDecisionParameters().get("source")[0].equals("-1"); //not triggered by rules
    }

    private void createChooseOptionForCardOptions(MultipleChoiceAwaitingDecision decision, DefaultLotroGame game) {
        int sourceCardId = Integer.parseInt(decision.getDecisionParameters().get("source")[0]);
        PhysicalCard sourceCard = game.getGameState().getPhysicalCard(sourceCardId);
        String[] legalOptions = decision.getDecisionParameters().get("results");

        for (int i = 0; i < legalOptions.length; i++) {
            options.add(new ChooseOptionForCardOption(sourceCard, legalOptions[i], i));
        }
    }

    private boolean isMoveAgainDecision(MultipleChoiceAwaitingDecision decision) {
        return decision.getText().equals(MOVE_AGAIN_TEXT);
    }

    private void createMoveAgainOptions(MultipleChoiceAwaitingDecision decision) {
        Map<String, String[]> params = decision.getDecisionParameters();
        String[] legalOptions = params.get("results");

        for (int i = 0; i < legalOptions.length; i++) {
            if (legalOptions[i].equalsIgnoreCase("yes")) {
                options.add(new MoveAgainOption(true, i));
            } else if (legalOptions[i].equalsIgnoreCase("no")) {
                options.add(new MoveAgainOption(false, i));
            } else {
                throw new IllegalArgumentException("Unexpected option for move again decision: " + legalOptions[i]);
            }
        }
    }

    private boolean isMulliganDecision(MultipleChoiceAwaitingDecision decision) {
        return decision.getText().equals(MULLIGAN_TEXT);
    }

    private void createMulliganOptions(MultipleChoiceAwaitingDecision decision) {
        Map<String, String[]> params = decision.getDecisionParameters();
        String[] legalOptions = params.get("results");

        for (int i = 0; i < legalOptions.length; i++) {
            if (legalOptions[i].equalsIgnoreCase("yes")) {
                options.add(new MulliganOption(true, i));
            } else if (legalOptions[i].equalsIgnoreCase("no")) {
                options.add(new MulliganOption(false, i));
            } else {
                throw new IllegalArgumentException("Unexpected option for mulligan decision: " + legalOptions[i]);
            }
        }
    }

    private boolean isChooseStartingPositionDecision(MultipleChoiceAwaitingDecision decision) {
        Map<String, String[]> params = decision.getDecisionParameters();
        String[] legalOptions = params.get("results");

        return decision.getText().equals(CHOOSE_STARTING_POSITION_TEXT) && legalOptions.length == 2 && legalOptions[0].equals("Go first") && legalOptions[1].equals("Go second");
    }

    private void createChooseStartingPositionOptions(MultipleChoiceAwaitingDecision decision) {
        Map<String, String[]> params = decision.getDecisionParameters();
        String[] legalOptions = params.get("results");

        for (int i = 0; i < legalOptions.length; i++) {
            options.add(new StartingPositionOption(legalOptions[i], i));
        }
    }

    @Override
    public List<Option> getOptions() {
        return options;
    }
}
