package com.gempukku.lotro.bots.forge.plan;

import com.gempukku.lotro.bots.forge.plan.action.ActionToTake;
import com.gempukku.lotro.bots.forge.plan.action.PassAction;
import com.gempukku.lotro.bots.forge.plan.action.PlayCardFromHandAction;
import com.gempukku.lotro.cards.build.bot.abstractcard.BotCard;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.state.PlannedBoardState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

import java.util.*;

public class ShadowPlan {
    private final int siteNumber;
    private final String playerName;
    private final LotroGame game;
    private final boolean printDebugMessages;

    private int nextStep = 0;
    List<ActionToTake> actions = new ArrayList<>();
    private final PlannedBoardState plannedBoardState;

    public ShadowPlan(boolean printDebugMessages, LotroGame game) {
        this.siteNumber = game.getGameState().getCurrentSiteNumber();
        this.game = game;
        this.printDebugMessages = printDebugMessages;

        if (printDebugMessages) {
            System.out.println("Making new shadow plan for opponent's site " + siteNumber);
        }

        plannedBoardState = new PlannedBoardState(game);
        this.playerName = plannedBoardState.getOpponent(game.getGameState().getCurrentPlayerId());
        makePlan();
    }

    private void makePlan() {
        List<ShadowPhaseEndState> allEndStates = new ArrayList<>(findAllShadowPhaseEndStates());

        for (ShadowPhaseEndState endState : allEndStates) {
            if (endState.hasPotentialToWinTheGame()) {
                this.actions = endState.getActions();
                if (printDebugMessages) {
                    System.out.println("Chosen shadow plan leading to potential win:");
                    for (ActionToTake action : actions) {
                        if (action instanceof PlayCardFromHandAction playCardFromHandAction) {
                            BotCard card = plannedBoardState.getCardById(playCardFromHandAction.getCard().getCardId());
                            if (!CardType.MINION.equals(card.getSelf().getBlueprint().getCardType())) {
                                throw new IllegalStateException("Only minion play is implemented in ShadowPlan");
                            }
                            System.out.println("Will play minion " + card.getSelf().getBlueprint().getFullName() + " from hand");
                        } else if (action instanceof PassAction){
                            System.out.println("Finally, will pass");
                        } else {
                            throw new IllegalStateException("Only PlayCardFromHandAction and PassAction is implemented in ShadowPlan");
                        }
                    }
                }
                return;
            }
        }

    }

    private Set<ShadowPhaseEndState> findAllShadowPhaseEndStates() {
        Set<ShadowPhaseEndState> endStates = new HashSet<>();
        explore(plannedBoardState, new ArrayList<>(), endStates);
        return endStates;
    }

    private void explore(PlannedBoardState plannedBoardState, ArrayList<ActionToTake> history, Set<ShadowPhaseEndState> endStates) {
        List<ActionToTake> possibleActions = getAllPossibleShadowActions(plannedBoardState);

        for (ActionToTake action : possibleActions) {
            PlannedBoardState next = new PlannedBoardState(plannedBoardState);
            history.add(action);
            if (action instanceof PassAction) {
                ShadowPhaseEndState endState = new ShadowPhaseEndState(next, history);
                endStates.add(endState);
            } else {
                if (action instanceof PlayCardFromHandAction playCardFromHandAction) {
                    BotCard cardToPlay = next.getCardById(playCardFromHandAction.getCard().getCardId());
                    if (cardToPlay.getSelf().getBlueprint().getCardType().equals(CardType.MINION)) {
                        next.playMinion(cardToPlay);
                    } else {
                        throw new IllegalStateException("Only minion play is implemented in ShadowPlan");
                    }
                } else {
                    throw new IllegalStateException("Only PlayCardFromHandAction is implemented in ShadowPlan");
                }
                explore(next, history, endStates);
            }
            history.removeLast();
        }
    }

    private List<ActionToTake> getAllPossibleShadowActions(PlannedBoardState plannedBoardState) {
        List<ActionToTake> possibleActions = new ArrayList<>();

        List<BotCard> shadowCardsInHand = plannedBoardState.getHand(playerName).stream()
                .filter(botCard -> Side.SHADOW.equals(botCard.getSelf().getBlueprint().getSide()))
                .toList();

        for (BotCard botCard : shadowCardsInHand) {
            if (botCard.getSelf().getBlueprint().getCardType().equals(CardType.MINION)) {
                int currentSiteNumber = plannedBoardState.getCurrentSite().getSelf().getBlueprint().getSiteNumber();
                int minionSiteNumber = botCard.getSelf().getBlueprint().getSiteNumber();
                boolean roaming = minionSiteNumber > currentSiteNumber;
                int twilightCost = botCard.getSelf().getBlueprint().getTwilightCost();
                if (roaming) {
                    twilightCost += 2;
                }
                if (plannedBoardState.getTwilight() >= twilightCost) {
                    possibleActions.add(new PlayCardFromHandAction(botCard.getSelf()));
                }
            }
        }

        possibleActions.add(new PassAction());

        return possibleActions;
    }

    public int chooseActionToTakeOrPass(AwaitingDecision awaitingDecision) {
        if (printDebugMessages) {
            System.out.println("Shadow plan asked to take action on " + awaitingDecision.toJson().toString());
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
            System.out.println("Action " + (nextStep + 1) + " out of " + actions.size());
            System.out.println(action.toString());
        }
        nextStep++;
        return action.carryOut(awaitingDecision);
    }

    public List<PhysicalCard> chooseTarget(AwaitingDecision awaitingDecision) {
        //TODO
        throw new IllegalStateException("Targeting not implemented yet in ShadowPlan");
    }

    public boolean replanningNeeded() {
        return !isActive();
    }

    private boolean isActive() {
        boolean tbr =  !game.getGameState().getCurrentPlayerId().equals(playerName)
                && game.getGameState().getCurrentSiteNumber() == siteNumber;
        if (printDebugMessages) {
            System.out.println("Plan is active: " + tbr);
        }
        return tbr;
    }
}
