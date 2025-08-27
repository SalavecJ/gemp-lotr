package com.gempukku.lotro.bots.forge.plan;

import com.gempukku.lotro.bots.forge.plan.action.ActionToTake;
import com.gempukku.lotro.bots.forge.plan.action.DiscardCompanionToHealAction;
import com.gempukku.lotro.bots.forge.plan.action.ReplanAction;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class FellowshipPhasePlan {
    private final int siteNumber;
    private final String playerName;
    private final LotroGame game;
    private final boolean printDebugMessages;

    private int nextStep = 0;
    List<ActionToTake> actions = new ArrayList<>();

    public FellowshipPhasePlan(boolean printDebugMessages, LotroGame game) {
        this.siteNumber = game.getGameState().getCurrentSiteNumber();
        this.playerName = game.getGameState().getCurrentPlayerId();
        this.game = game;
        this.printDebugMessages = printDebugMessages;

        if (printDebugMessages) {
            System.out.println("Making new fellowship phase plan");
        }

        makePlan();
    }

    private void makePlan() {
//        List<PhysicalCard> inPlayWithActivatedAbility = (List<PhysicalCard>) game.getGameState().getInPlay().stream().filter((Predicate<PhysicalCard>) card -> CardDatabase.getCard(card.getBlueprintId()).hasActivatedAbilities(Phase.FELLOWSHIP)).toList();
//        List<PhysicalCard> inHandPlayableInFellowshipPhase = (List<PhysicalCard>) game.getGameState().getHand(playerName).stream().filter((Predicate<PhysicalCard>) card -> CardDatabase.getCard(card.getBlueprintId()).isPlayableInPhase(Phase.FELLOWSHIP)).toList();
//
//        addDiscardCompanionToHealActions(inHandPlayableInFellowshipPhase);
//        addPlayCompanionFromHandActions(inHandPlayableInFellowshipPhase);

    }

    private void addPlayCompanionFromHandActions(List<PhysicalCard> inHandPlayableInFellowshipPhase) {
        // TODO implement

    }

    private void addDiscardCompanionToHealActions(List<PhysicalCard> inHandPlayableInFellowshipPhase) {
        List<PhysicalCard> woundedUniqueCompanionsInPlay = (List<PhysicalCard>) game.getGameState().getInPlay().stream()
                .filter((Predicate<PhysicalCard>) card ->
                        CardType.COMPANION.equals(card.getBlueprint().getCardType())
                        && card.getBlueprint().isUnique()
                        && game.getGameState().getWounds(card) > 0
                        && game.getModifiersQuerying().canBeHealed(game, card))
                .toList();

        for (PhysicalCard companion : woundedUniqueCompanionsInPlay) {
            int wounds = game.getGameState().getWounds(companion);

            List<PhysicalCard> matchingCardsInHand = inHandPlayableInFellowshipPhase.stream()
                    .filter(cardInHand ->
                            CardType.COMPANION.equals(cardInHand.getBlueprint().getCardType())
                            && cardInHand.getBlueprint().getTitle().equals(companion.getBlueprint().getTitle()))
                    .toList();

            int cardsToDiscardToHeal = Math.min(wounds, matchingCardsInHand.size());

            for (int i = 0; i < cardsToDiscardToHeal; i++) {
                PhysicalCard toDiscard = matchingCardsInHand.get(i);
                if (printDebugMessages) {
                    System.out.println("Will discard " + toDiscard.getBlueprint().getFullName() + " from hand to heal companion in play");
                }
                actions.add(new DiscardCompanionToHealAction(toDiscard));
                inHandPlayableInFellowshipPhase.remove(toDiscard);
            }
        }
    }

    public int chooseActionToTakeOrPass(AwaitingDecision awaitingDecision) {
        if (printDebugMessages) {
            System.out.println("Fellowship phase plan asked to take action on " + awaitingDecision.toJson().toString());
        }

        if (!isActive()) {
            if (printDebugMessages) {
                System.out.println("Plan is outdated");
            }
            throw new IllegalStateException("Plan is outdated");
        }

        if (nextStep >= actions.size()) {
            System.out.println("All actions from plan already taken");
            throw new IllegalStateException("All actions from plan already taken");
        }

        ActionToTake action = actions.get(nextStep);
        if (printDebugMessages) {
            System.out.println("Action " + nextStep + " out of " + actions.size());
            System.out.println(action.toString());
        }
        nextStep++;
        return action.carryOut(awaitingDecision);
    }

    public boolean replanningNeeded() {
        return !isActive() || nextStep >= actions.size() || actions.get(nextStep) instanceof ReplanAction;
    }

    private boolean isActive() {
        boolean tbr =  game.getGameState().getCurrentPlayerId().equals(playerName)
                && game.getGameState().getCurrentPhase().equals(Phase.FELLOWSHIP)
                && game.getGameState().getCurrentSiteNumber() == siteNumber;
        if (printDebugMessages) {
            System.out.println("Plan is active: " + tbr);
        }
        return tbr;
    }
}
