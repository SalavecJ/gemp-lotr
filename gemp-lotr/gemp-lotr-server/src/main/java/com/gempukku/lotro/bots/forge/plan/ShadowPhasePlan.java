package com.gempukku.lotro.bots.forge.plan;

import com.gempukku.lotro.bots.forge.plan.action2.ActionToTake2;
import com.gempukku.lotro.bots.forge.plan.action2.ChooseTargetsAction2;
import com.gempukku.lotro.bots.forge.plan.endstate.PhaseEndState;
import com.gempukku.lotro.bots.forge.plan.endstate.ShadowPhaseEndState;
import com.gempukku.lotro.bots.forge.utils.ActionFinderUtil;
import com.gempukku.lotro.bots.forge.utils.BoardStateUtil;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.DefaultUserFeedback;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.*;

public class ShadowPhasePlan {
    private final int siteNumber;
    private final String playerName;
    private final LotroGame game;
    private final boolean printDebugMessages;

    private int nextStep = 0;
    List<ActionToTake2> actions2 = new ArrayList<>();

    private final DefaultLotroGame copy;

    public ShadowPhasePlan(boolean printDebugMessages, DefaultLotroGame game) {
        this.siteNumber = game.getGameState().getCurrentSiteNumber();
        this.game = game;
        this.printDebugMessages = printDebugMessages;

        if (printDebugMessages) {
            System.out.println("Making new shadow phase plan for opponent's site " + siteNumber);
        }

        this.playerName = game.getGameState().getPlayerNames().stream().filter(s -> !s.equals(game.getGameState().getCurrentPlayerId())).findFirst().orElseThrow();

        copy = game.getCopyByReplayingDecisionsFromStart(new DefaultUserFeedback());
        makePlan();
    }

    private void makePlan() {
        List<ShadowPhaseEndState> allEndStates = new ArrayList<>(ActionFinderUtil.findAllShadowPhaseEndStates(copy));

        if (printDebugMessages) {
            System.out.println("Total shadow end states found: " + allEndStates.size());
        }

        // Select only interesting end states using heuristics (for performance)
        List<ShadowPhaseEndState> interestingEndStates = selectInterestingEndStates(allEndStates);

        if (printDebugMessages) {
            System.out.println("Interesting shadow end states selected: " + interestingEndStates.size());
        }

        // Check for winning play
        interestingEndStates.stream().filter(ShadowPhaseEndState::hasPotentialToWinTheGame)
                .max(Comparator.comparingInt(this::fpLosesBeforeSkirmish) // Prefer winning before skirmish
                        .thenComparingInt(this::countMinionsOnBoard)) // Prefer winning with more minions on board
                .ifPresent(shadowPhaseEndState -> {
            actions2 = shadowPhaseEndState.getShadowActions();
            if (printDebugMessages) {
                System.out.println("Chosen shadow plan leading to potential win:");
                System.out.println(shadowPhaseEndState);
            }
        });

        // If no winning play, choose best evaluated play
        if (actions2.isEmpty()) {
            interestingEndStates.stream().max(Comparator.comparingDouble(PhaseEndState::getValue)).ifPresent(bestEndState -> {
                this.actions2 = bestEndState.getShadowActions();
                if (printDebugMessages) {
                    System.out.println("No shadow plan leads to win, chosen best plan with value " + bestEndState.getValue());
                    System.out.println(bestEndState);
                }
            });
        }
    }

    /**
     * Selects a diverse set of interesting shadow end states using heuristics.
     * This avoids evaluating all possible end states for better performance.
     */
    private List<ShadowPhaseEndState> selectInterestingEndStates(List<ShadowPhaseEndState> allEndStates) {
        if (allEndStates.isEmpty()) {
            return allEndStates;
        }

        int minionsAtStart = BoardStateUtil.getMinionsInPlay(copy).size();

        Set<ShadowPhaseEndState> selected = new LinkedHashSet<>();

        // Strategy 1: Do nothing (save everything for next turn)
        findEndStateWithNoActions(allEndStates).ifPresent(selected::add);

        // Strategy 2: Play the strongest board with given number of minions
        for (int i = minionsAtStart; true; i++) {
            Optional<ShadowPhaseEndState> endState = findEndStateWithStrongestMinions(allEndStates, i);
            if (endState.isPresent()) {
                selected.add(endState.get());
            } else {
                break;
            }
        }

        // Strategy 3: Play archers
        findEndStateWithMostArchery(allEndStates).ifPresent(selected::add);


        return new ArrayList<>(selected);
    }

    private Optional<ShadowPhaseEndState> findEndStateWithNoActions(List<ShadowPhaseEndState> endStates) {
        return endStates.stream().filter(shadowPhaseEndState -> shadowPhaseEndState.getShadowActions().size() == 1).findFirst();
    }

    private Optional<ShadowPhaseEndState> findEndStateWithStrongestMinions(List<ShadowPhaseEndState> endStates, int numberOfMinions) {
        return endStates.stream()
                .filter(shadowPhaseEndState -> countMinionsOnBoard(shadowPhaseEndState) == numberOfMinions)
                .max(Comparator.comparingInt(this::countTotalStrengthOfMinionsOnBoard)
                        .thenComparingInt(this::countShadowConditionsOnBoard));
    }

    private Optional<ShadowPhaseEndState> findEndStateWithMostArchery(List<ShadowPhaseEndState> endStates) {
        return endStates.stream()
                .filter(shadowPhaseEndState -> countArcherMinionsOnBoard(shadowPhaseEndState) > 0)
                .max(Comparator.comparingInt(this::countArcherMinionsOnBoard)
                        .thenComparingInt(this::countShadowConditionsOnBoard));
    }

    private int countMinionsOnBoard(ShadowPhaseEndState endState) {
        return Math.toIntExact(endState.getGameCopy().getGameState().getActiveCards().stream().filter(card -> CardType.MINION.equals(card.getBlueprint().getCardType())).count());
    }

    private int countArcherMinionsOnBoard(ShadowPhaseEndState endState) {
        return Math.toIntExact(endState.getGameCopy().getGameState().getActiveCards().stream()
                .filter(card -> CardType.MINION.equals(card.getBlueprint().getCardType())
                        && endState.getGameCopy().getModifiersQuerying().hasKeyword(endState.getGameCopy(), card, Keyword.ARCHER))
                .count());
    }

    private int countShadowConditionsOnBoard(ShadowPhaseEndState endState) {
        return Math.toIntExact(endState.getGameCopy().getGameState().getActiveCards().stream()
                .filter(card -> CardType.CONDITION.equals(card.getBlueprint().getCardType())
                        && Side.SHADOW == card.getBlueprint().getSide())
                .count());
    }

    private int fpLosesBeforeSkirmish(ShadowPhaseEndState endState) {
        if (endState.getGameCopy().getGameState().getCurrentPhase() == Phase.ARCHERY
                || endState.getGameCopy().getGameState().getCurrentPhase() == Phase.MANEUVER) {
            return 1;
        }
        return 0;
    }

    private int countTotalStrengthOfMinionsOnBoard(ShadowPhaseEndState endState) {
        return endState.getGameCopy().getGameState().getActiveCards().stream()
                .filter(card -> CardType.MINION.equals(card.getBlueprint().getCardType()))
                .mapToInt(card -> endState.getGameCopy().getModifiersQuerying().getStrength(endState.getGameCopy(), card)).sum();
    }

    public String chooseActionToTakeOrPass(AwaitingDecision awaitingDecision) {
        if (printDebugMessages) {
            System.out.println("Shadow plan asked to take action on " + awaitingDecision.toJson().toString());
        }

        if (!isActive()) {
            if (printDebugMessages) {
                System.out.println("Plan is outdated");
            }
            throw new IllegalStateException("Plan is outdated");
        }

        if (nextStep >= actions2.size()) {
            System.out.println("All actions from plan already taken");
            throw new IllegalStateException("All actions from plan already taken");
        }

        ActionToTake2 action = actions2.get(nextStep);
        if (!action.getDecisionText().equals(awaitingDecision.getText())) {
            throw new IllegalStateException("Next action in plan does not match the decision asked: expected "
                    + action.getDecisionText() + " but got " + awaitingDecision.getText());
        }
        if (printDebugMessages) {
            System.out.println("Action " + (nextStep + 1) + " out of " + actions2.size());
            System.out.println(action);
        }
        String result = action.carryOut();
        nextStep++;
        return result;
    }

    public String chooseTarget(AwaitingDecision awaitingDecision) {
        if (printDebugMessages) {
            System.out.println("Shadow phase plan asked to take action on " + awaitingDecision.toJson().toString());
        }

        if (!isActive()) {
            if (printDebugMessages) {
                System.out.println("Plan is outdated");
            }
            throw new IllegalStateException("Plan is outdated");
        }

        if (actions2.isEmpty()) {
            if (printDebugMessages) {
                System.out.println("No actions in plan");
            }
            throw new IllegalStateException("No actions in plan");
        }

        if (nextStep >= actions2.size()) {
            System.out.println("All actions from plan already taken");
            throw new IllegalStateException("All actions from plan already taken");
        }

        ActionToTake2 action = actions2.get(nextStep);
        if (!action.getDecisionText().equals(awaitingDecision.getText())) {
            throw new IllegalStateException("Next action in plan does not match the decision asked: expected "
                    + action.getDecisionText() + " but got " + awaitingDecision.getText());
        }
        if (!(action instanceof ChooseTargetsAction2)) {
            throw new IllegalStateException("Next action in plan is not target action");
        }
        if (printDebugMessages) {
            System.out.println("Action " + (nextStep + 1) + " out of " + actions2.size());
            System.out.println(action);
        }
        nextStep++;
        return action.carryOut();
    }

    public boolean replanningNeeded() {
        return !isActive();
    }

    private boolean isActive() {
        return !game.getGameState().getCurrentPlayerId().equals(playerName)
                && game.getGameState().getCurrentSiteNumber() == siteNumber
                && game.getGameState().getCurrentPhase().equals(Phase.SHADOW);
    }
}
