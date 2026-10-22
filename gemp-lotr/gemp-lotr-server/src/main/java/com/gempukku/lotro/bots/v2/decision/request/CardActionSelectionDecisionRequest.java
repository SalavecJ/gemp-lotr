package com.gempukku.lotro.bots.v2.decision.request;

import com.gempukku.lotro.bots.v2.decision.option.*;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.decisions.CardActionSelectionDecision;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CardActionSelectionDecisionRequest extends DecisionRequest {
    private static final String OPTIONAL_TRIGGER_RESPONSES_TEXT = "Optional responses";
    private static final String RESPONSES_KEYWORD_SUFFIX = " - Optional \"is about to\" responses";
    private static final String FELLOWSHIP_TEXT = "Play Fellowship action or Pass";
    private static final String SHADOW_TEXT = "Play Shadow action or Pass";
    private static final String MANEUVER_TEXT = "Play Maneuver action or Pass";
    private static final String SKIRMISH_TEXT = "Choose action to play or Pass";
    private static final String REGROUP_TEXT = "Play Regroup action or Pass";

    private static final String PLAY_CARD_PREFIX = "Play";
    private static final String USE_ABILITY_PREFIX = "Use";
    private static final String HEAL_TO_DISCARD_TEXT = "Heal by discarding";
    private static final String TRANSFER_PREFIX = "Transfer";
    private static final String OPTIONAL_RESPONSE_PREFIX = "Optional trigger from";

    private final List<Option> options = new ArrayList<>();

    public CardActionSelectionDecisionRequest(CardActionSelectionDecision decision, DefaultLotroGame game) {
        super(decision.getText());

        Map<String, String[]> params = decision.getDecisionParameters();
        String[] actionIds = params.get("actionId");
        if (actionIds == null || actionIds.length == 0) {
            // No actions available: must pass
            options.add(new PassOption());
            return;
        }

        if (isPlayCardOrUseAbilityAction(decision)) {
            createPlayCardOrUseAbilityOptions(decision, game);
        } else {
            throw new UnsupportedOperationException("Card action selection decision request not supported: " + decision.toJson().toString());
        }
    }

    private boolean isPlayCardOrUseAbilityAction(CardActionSelectionDecision decision) {
        return decision.getText().equals(OPTIONAL_TRIGGER_RESPONSES_TEXT) ||
                decision.getText().endsWith(RESPONSES_KEYWORD_SUFFIX) ||
                decision.getText().equals(FELLOWSHIP_TEXT) ||
                decision.getText().equals(SHADOW_TEXT) ||
                decision.getText().equals(MANEUVER_TEXT) ||
                decision.getText().equals(SKIRMISH_TEXT) ||
                decision.getText().equals(REGROUP_TEXT);
    }

    private void createPlayCardOrUseAbilityOptions(CardActionSelectionDecision decision, DefaultLotroGame game) {
        Map<String, String[]> params = decision.getDecisionParameters();
        String[] physicalIds = params.get("cardId");
        String[] actionTexts = params.get("actionText");

        for (int i = 0; i < physicalIds.length; i++) {
            String actionText = actionTexts[i];
            PhysicalCard card = game.getGameState().getPhysicalCard(Integer.parseInt(physicalIds[i]));
            if (actionText.startsWith(PLAY_CARD_PREFIX)) {
                options.add(new PlayCardOption(card, i));
            } else if (actionText.startsWith(USE_ABILITY_PREFIX) || actionText.startsWith(OPTIONAL_RESPONSE_PREFIX)) {
                options.add(new ActivateCardOption(card, i));
            } else if (actionText.equals(HEAL_TO_DISCARD_TEXT)) {
                options.add(new DiscardCardToHealOption(card, i));
            } else if (actionText.startsWith(TRANSFER_PREFIX)) {
                options.add(new TransferCardOption(card, i));
            } else {
                throw new UnsupportedOperationException("Unsupported action text '" + actionText + "' for decision: " + decision.toJson().toString());
            }
        }

        options.add(new PassOption());
    }

    @Override
    public List<Option> getOptions() {
        return options;
    }
}
