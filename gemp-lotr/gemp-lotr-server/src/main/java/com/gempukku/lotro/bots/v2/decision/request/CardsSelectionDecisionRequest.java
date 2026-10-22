package com.gempukku.lotro.bots.v2.decision.request;

import com.gempukku.lotro.bots.v2.decision.option.*;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.decisions.CardsSelectionDecision;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CardsSelectionDecisionRequest extends DecisionRequest {
    private static final String RECONCILE_TEXT = "Reconcile - choose card to discard or press DONE";
    private static final String RECONCILE_FORCED_TEXT = "Choose cards to discard down to 8";
    private static final String NEXT_SKIRMISH_TEXT = "Choose next skirmish to resolve";
    private static final String SHADOW_ASSIGN_ARCHERY_TEXT_PREFIX = "Choose minion to assign archery wound to - remaining wounds:";
    private static final String FP_ASSIGN_ARCHERY_TEXT_PREFIX = "Choose character to assign archery wound to - remaining wounds:";
    private static final String SANCTUARY_TEXT_PREFIX = "Sanctuary healing - Choose companion to heal - remaining heals:";

    private int min;
    private int max;

    private final List<Option> options = new ArrayList<>();

    public CardsSelectionDecisionRequest(CardsSelectionDecision decision, DefaultLotroGame game) {
        super(decision.getText());

        Map<String, String[]> params = decision.getDecisionParameters();
        String[] cardIds = params.get("cardId");
        min = params.containsKey("min") ? Integer.parseInt(params.get("min")[0]) : 0;
        max = params.containsKey("max") ? Integer.parseInt(params.get("max")[0]) : cardIds.length;

        if (min == 0) {
            min = 1; // Explicit pass option added
        }

        // If nothing is selectable or max == 0, we have no options
        if (cardIds.length == 0 || max == 0) {
            options.add(new PassOption());
            max = 1; // Explicit pass option added
            return;
        }

        if (isReconcileDecision(decision)) {
            createReconcileOptions(decision, game);
        } else if (isNextSkirmishDecision(decision)) {
            createNextSkirmishOptions(decision, game);
        } else if (isArcheryAssignmentDecision(decision)) {
            createArcheryAssignmentOptions(decision, game);
        } else if (isSanctuaryHealDecision(decision)) {
            createSanctuaryHealOptions(decision, game);
        } else if (isChooseTargetForCardDecision(decision)) {
            createChooseTargetForCardOptions(decision, game);
        } else {
            throw new UnsupportedOperationException("Cards selection decision request not supported: " + decision.toJson().toString());
        }
    }

    private boolean isSanctuaryHealDecision(CardsSelectionDecision decision) {
        return decision.getText().startsWith(SANCTUARY_TEXT_PREFIX);
    }

    private void createSanctuaryHealOptions(CardsSelectionDecision decision, DefaultLotroGame game) {
        Map<String, String[]> params = decision.getDecisionParameters();
        String[] physicalIds = params.get("cardId");

        for (String physicalId : physicalIds) {
            PhysicalCard card = game.getGameState().getPhysicalCard(Integer.parseInt(physicalId));
            options.add(new SanctuaryHealOption(card));
        }

        options.add(new PassOption());
    }

    private boolean isChooseTargetForCardDecision(CardsSelectionDecision decision) {
        Map<String, String[]> params = decision.getDecisionParameters();
        String source = params.get("source")[0];

        return !source.equals("-1");
    }

    private void createChooseTargetForCardOptions(CardsSelectionDecision decision, DefaultLotroGame game) {
        Map<String, String[]> params = decision.getDecisionParameters();
        String[] physicalIds = params.get("cardId");
        PhysicalCard sourceCard = game.getGameState().getPhysicalCard(Integer.parseInt(params.get("source")[0]));

        for (String physicalId : physicalIds) {
            PhysicalCard card = game.getGameState().getPhysicalCard(Integer.parseInt(physicalId));
            options.add(new ChooseTargetForCardOption(sourceCard, card));
        }
    }

    private boolean isArcheryAssignmentDecision(CardsSelectionDecision decision) {
        return decision.getText().startsWith(SHADOW_ASSIGN_ARCHERY_TEXT_PREFIX) || decision.getText().startsWith(FP_ASSIGN_ARCHERY_TEXT_PREFIX);
    }

    private void createArcheryAssignmentOptions(CardsSelectionDecision decision, DefaultLotroGame game) {
        Map<String, String[]> params = decision.getDecisionParameters();
        String[] physicalIds = params.get("cardId");

        for (String physicalId : physicalIds) {
            PhysicalCard card = game.getGameState().getPhysicalCard(Integer.parseInt(physicalId));
            options.add(new ArcheryWoundOption(card));
        }
    }

    private boolean isNextSkirmishDecision(CardsSelectionDecision decision) {
        return decision.getText().equals(NEXT_SKIRMISH_TEXT);
    }

    private void createNextSkirmishOptions(CardsSelectionDecision decision, DefaultLotroGame game) {
        Map<String, String[]> params = decision.getDecisionParameters();
        String[] physicalIds = params.get("cardId");

        for (String physicalId : physicalIds) {
            PhysicalCard card = game.getGameState().getPhysicalCard(Integer.parseInt(physicalId));
            options.add(new NextSkirmishOption(card, game));
        }
    }

    private boolean isReconcileDecision(CardsSelectionDecision decision) {
        return decision.getText().equals(RECONCILE_TEXT) || decision.getText().equals(RECONCILE_FORCED_TEXT);
    }

    private void createReconcileOptions(CardsSelectionDecision decision, DefaultLotroGame game) {
        Map<String, String[]> params = decision.getDecisionParameters();
        String[] physicalIds = params.get("cardId");

        for (String physicalId : physicalIds) {
            PhysicalCard card = game.getGameState().getPhysicalCard(Integer.parseInt(physicalId));
            options.add(new DiscardToReconcileOption(card));
        }

        if (decision.getText().equals(RECONCILE_TEXT)) {
            // Pass option is only available if the player is not forced to discard down to 8 cards
            options.add(new PassOption());
        }
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
