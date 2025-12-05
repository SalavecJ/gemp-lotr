package com.gempukku.lotro.bots.forge.plan;

import com.gempukku.lotro.bots.forge.plan.action2.ActionToTake2;
import com.gempukku.lotro.bots.forge.plan.endstate.AfterCombatEndState;
import com.gempukku.lotro.bots.forge.utils.ActionFinderUtil;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.DefaultUserFeedback;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.*;

public class CombatPhasesPlan {
    private final int siteNumber;
    private final String playerName;
    private final LotroGame game;
    private final boolean printDebugMessages;

    List<ActionToTake2> actions2 = new ArrayList<>();
    List<ActionToTake2> opponentActions = new ArrayList<>();

    private boolean planMade = false;

    public CombatPhasesPlan(boolean printDebugMessages, DefaultLotroGame game, String playerName) {
        this.siteNumber = game.getGameState().getCurrentSiteNumber();
        this.game = game;
        this.printDebugMessages = printDebugMessages;

        if (printDebugMessages) {
            System.out.println("New combat plan for site " + siteNumber);
        }

        this.playerName = playerName;

    }

    public CombatPhasesPlan(boolean printDebugMessages, DefaultLotroGame game, String playerName,
                            List<ActionToTake2> myActions, List<ActionToTake2> opponentActions) {
        this.siteNumber = game.getGameState().getCurrentSiteNumber();
        this.game = game;
        this.printDebugMessages = printDebugMessages;

        if (printDebugMessages) {
            System.out.println("New combat plan for site " + siteNumber + " from provided actions");
        }

        this.playerName = playerName;

        this.actions2 = new ArrayList<>(myActions);
        this.opponentActions = new ArrayList<>(opponentActions);

        planMade = true;
    }

    private void makePlan(DefaultLotroGame game) {
        DefaultLotroGame copy = game.getCopyByReplayingDecisionsFromStart(new DefaultUserFeedback());
        AfterCombatEndState endPhase = ActionFinderUtil.findBestCombatPath(copy);
        if (copy.getGameState().getCurrentPlayerId().equals(playerName)) {
            actions2 = endPhase.getFpActions();
            opponentActions = endPhase.getShadowActions();
        } else {
            actions2 = endPhase.getShadowActions();
            opponentActions = endPhase.getFpActions();
        }
        if (printDebugMessages) {
            System.out.println("Planned actions:");
            for (int i = 1; i <= actions2.size(); i++) {
                System.out.println("  " + i + ". " + actions2.get(i - 1));
            }
            System.out.println("Planned opponent actions:");
            for (int i = 1; i <= opponentActions.size(); i++) {
                System.out.println("  " + i + ". " + opponentActions.get(i - 1));
            }
        }
        planMade = true;
    }

    public String chooseActionToTakeOrPass(DefaultLotroGame game, AwaitingDecision awaitingDecision) {
        if (printDebugMessages) {
            System.out.println("Combat phases plan asked to take action on " + awaitingDecision.toJson().toString());
        }

        if (isOutdated()) {
            if (awaitingDecision.getDecisionType().equals(AwaitingDecisionType.CARD_ACTION_CHOICE)
                    && awaitingDecision.getDecisionParameters().get("cardId").length == 0) {
                if (printDebugMessages) {
                    System.out.println("Only possible action is passing, plan later");
                }
                return "";
            }
            if (printDebugMessages) {
                System.out.println("Planning...");
            }
            makePlan(game);
        }

        ActionToTake2 action = actions2.removeFirst();
        if (!action.getDecisionText().equals(awaitingDecision.getText())) {
            throw new IllegalStateException("Next action in plan does not match the decision asked: expected "
                    + action.getDecisionText() + " but got " + awaitingDecision.getText());
        }
        if (printDebugMessages) {
            System.out.println(action);
        }
        return action.carryOut();
    }

    public void decisionMadeByPlayer(DefaultLotroGame game, AwaitingDecision awaitingDecision, String answer, String player) {
        // check if opponent's actions have invalidated the plan
        if (planMade && !player.equals(playerName)) {
            if (opponentActions.isEmpty()) {
                if (printDebugMessages) {
                    System.out.println("Expected no opponent action, invalidating the plan. " + awaitingDecision.toJson());
                }
                actions2.clear();
                planMade = false;
            } else if (opponentActions.getFirst().getDecisionText().equals(awaitingDecision.getText())
                    && opponentActions.getFirst().carryOut().equals(answer)) {
                opponentActions.removeFirst();
            } else {
                if (printDebugMessages) {
                    System.out.println("Opponent action does not match the plan, invalidating the plan. Expected "
                            + opponentActions.getFirst() + " but got " + awaitingDecision.toJson() + " with answer " + answer);
                }
                actions2.clear();
                opponentActions.clear();
                planMade = false;
            }
        }
    }

    public boolean isOutdated() {
        return actions2.isEmpty() || !areSiteAndPhaseCorrect();
    }

    public boolean areSiteAndPhaseCorrect() {
        return game.getGameState().getCurrentSiteNumber() == siteNumber
                && (game.getGameState().getCurrentPhase().equals(Phase.MANEUVER)
                || game.getGameState().getCurrentPhase().equals(Phase.ARCHERY)
                || game.getGameState().getCurrentPhase().equals(Phase.ASSIGNMENT)
                || game.getGameState().getCurrentPhase().equals(Phase.SKIRMISH)
                || game.getGameState().getCurrentPhase().equals(Phase.REGROUP));
    }
}
