package com.gempukku.lotro.bots.forge.plan;

import com.gempukku.lotro.bots.forge.cards.BotCardFactory;
import com.gempukku.lotro.bots.forge.cards.ability2.TriggeredAbility;
import com.gempukku.lotro.bots.forge.cards.abstractcard.BotCard;
import com.gempukku.lotro.bots.forge.plan.action2.AcceptTriggerAction2;
import com.gempukku.lotro.bots.forge.plan.action2.ActionToTake2;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.*;
import java.util.stream.Collectors;

public class BetweenTurnsPlan {
    private final int siteNumber;
    private final String playerName;
    private final LotroGame game;
    private final boolean printDebugMessages;

    public BetweenTurnsPlan(boolean printDebugMessages, LotroGame game) {
        this.siteNumber = game.getGameState().getCurrentSiteNumber();
        this.playerName = game.getGameState().getCurrentPlayerId();
        this.game = game;
        this.printDebugMessages = printDebugMessages;

        if (printDebugMessages) {
            System.out.println("New between turns phase plan - player " + playerName + " is about to play at site " + siteNumber);
        }

    }

    public boolean replanningNeeded() {
        return !isActive();
    }

    private boolean isActive() {
        boolean tbr =  game.getGameState().getCurrentPlayerId().equals(playerName)
                && game.getGameState().getCurrentPhase().equals(Phase.BETWEEN_TURNS)
                && game.getGameState().getCurrentSiteNumber() == siteNumber;
        if (printDebugMessages) {
            System.out.println("Plan is active: " + tbr);
        }
        return tbr;
    }

    public String chooseActionToTakeOrPass(AwaitingDecision awaitingDecision, DefaultLotroGame game) {

        List<BotCard> cards = Arrays.stream(awaitingDecision.getDecisionParameters().get("cardId")).map(Integer::parseInt).map(integer -> BotCardFactory.create(game.getGameState().findCardById(integer))).toList();
        Map<BotCard, Integer> cardsWithPositions = new HashMap<>();
        for (int i = 0; i < cards.size(); i++) {
            cardsWithPositions.put(cards.get(i), i);
        }

        if (cards.stream().allMatch(botCard -> botCard.getTriggeredAbility() == null)) {
            throw new UnsupportedOperationException("Decision not supported: " + awaitingDecision.toJson().toString());
        }

        double maxValue = 0.0;
        BotCard chosenCard = null;

        for (BotCard botCard : cards) {
            TriggeredAbility triggeredAbility = botCard.getTriggeredAbility();
            if (triggeredAbility == null) {
                continue;
            }
            double value = triggeredAbility.getPossibleValue(playerName, game);
            if (value > maxValue) {
                maxValue = value;
                chosenCard = botCard;
            }
        }


        if (chosenCard == null) {
            String joined = cards.stream()
                    .map(t -> t.getSelf().getBlueprint().getFullName())
                    .collect(Collectors.joining("; "));
            if (printDebugMessages) {
                System.out.println("Will pass without using any triggered ability among: " + joined);
            }
            return "";
        } else {
            ActionToTake2 action = new AcceptTriggerAction2(awaitingDecision.getText(), chosenCard, String.valueOf(cardsWithPositions.get(chosenCard)));
            if (printDebugMessages) {
                System.out.println(action);
            }
            return action.carryOut();
        }
    }

    public String chooseTarget(AwaitingDecision awaitingDecision) {
        if (printDebugMessages) {
            System.out.println("Between turns plan asked to take action on " + awaitingDecision.toJson().toString());
        }

        throw new IllegalStateException("Target choosing not supported in between turns plan");
    }
}
