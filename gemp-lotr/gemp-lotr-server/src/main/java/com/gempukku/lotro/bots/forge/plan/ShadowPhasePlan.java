package com.gempukku.lotro.bots.forge.plan;

import com.gempukku.lotro.bots.forge.plan.action.*;
import com.gempukku.lotro.bots.forge.plan.endstate.ShadowPhaseEndState;
import com.gempukku.lotro.bots.forge.utils.ActionFinderUtil;
import com.gempukku.lotro.cards.build.bot.abstractcard.BotCard;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.state.PlannedBoardState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;

import java.util.*;

public class ShadowPhasePlan {
    private final int siteNumber;
    private final String playerName;
    private final LotroGame game;
    private final boolean printDebugMessages;

    private int nextStep = 0;
    List<ActionToTake> actions = new ArrayList<>();
    private final PlannedBoardState plannedBoardState;

    public ShadowPhasePlan(boolean printDebugMessages, LotroGame game) {
        this.siteNumber = game.getGameState().getCurrentSiteNumber();
        this.game = game;
        this.printDebugMessages = printDebugMessages;

        if (printDebugMessages) {
            System.out.println("Making new shadow phase plan for opponent's site " + siteNumber);
        }

        plannedBoardState = new PlannedBoardState(game);
        this.playerName = plannedBoardState.getOpponent(game.getGameState().getCurrentPlayerId());
        makePlan();
    }

    private void makePlan() {
        List<ShadowPhaseEndState> allEndStates = new ArrayList<>(ActionFinderUtil.findAllShadowPhaseEndStates(plannedBoardState));

        if (printDebugMessages) {
            System.out.println("Total shadow end states found: " + allEndStates.size());
        }

        // Select only interesting end states using heuristics (for performance)
        List<ShadowPhaseEndState> interestingEndStates = selectInterestingEndStates(allEndStates);

        if (printDebugMessages) {
            System.out.println("Interesting shadow end states selected: " + interestingEndStates.size());
        }

        // Evaluate only the interesting ones (combat path is computed lazily)
        for (ShadowPhaseEndState endState : interestingEndStates) {
            if (endState.hasPotentialToWinTheGame()) {
                this.actions = endState.getShadowActions();
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

    /**
     * Selects a diverse set of interesting shadow end states using heuristics.
     * This avoids evaluating all possible end states for better performance.
     * Strategies selected:
     * 1. Do nothing (save cards for next turn)
     * 2. Play maximum minions (all-in aggression)
     * 3. Play biggest/most expensive minion only
     * 4. Play all conditions first, then fill with minions
     * 5. Play smallest/cheapest minions (keep twilight pool small)
     * 6. Balanced approach (medium number of minions)
     */
    private List<ShadowPhaseEndState> selectInterestingEndStates(List<ShadowPhaseEndState> allEndStates) {
        if (allEndStates.isEmpty()) {
            return allEndStates;
        }

        Set<ShadowPhaseEndState> selected = new LinkedHashSet<>();

        // Strategy 1: Do nothing (save everything for next turn)
        findEndStateWithNoActions(allEndStates).ifPresent(selected::add);

        // Strategy 2: Play the strongest board with given number of minions
        for (int i = 0; true; i++) {
            Optional<ShadowPhaseEndState> endState = findEndStateWithStrongestMinions(allEndStates, i);
            if (endState.isPresent()) {
                selected.add(endState.get());
            } else {
                break;
            }
        }

        return new ArrayList<>(selected);
    }

    private Optional<ShadowPhaseEndState> findEndStateWithNoActions(List<ShadowPhaseEndState> endStates) {
        return endStates.stream().filter(shadowPhaseEndState -> shadowPhaseEndState.getShadowActions().size() == 1).findFirst();
    }

    private Optional<ShadowPhaseEndState> findEndStateWithStrongestMinions(List<ShadowPhaseEndState> endStates, int numberOfMinions) {
        return endStates.stream()
                .filter(shadowPhaseEndState -> countMinionsOnBoard(shadowPhaseEndState) == numberOfMinions)
                .max(Comparator.comparingInt(this::countTotalStrengthOfMinionsOnBoard));
    }

    private int countMinionsOnBoard(ShadowPhaseEndState endState) {
        return Math.toIntExact(endState.getBoardState().getShadowCardsInPlay(endState.getBoardState().getCurrentShadowPlayer()).stream().filter(botCard -> CardType.MINION.equals(botCard.getSelf().getBlueprint().getCardType())).count());
    }

    private int countTotalStrengthOfMinionsOnBoard(ShadowPhaseEndState endState) {
        return endState.getBoardState().getShadowCardsInPlay(endState.getBoardState().getCurrentShadowPlayer()).stream()
                .filter(botCard -> CardType.MINION.equals(botCard.getSelf().getBlueprint().getCardType()))
                .mapToInt(botCard -> endState.getBoardState().getStrength(botCard)).sum();
    }

    private int countTotalVitalityOfMinionsOnBoard(ShadowPhaseEndState endState) {
        return endState.getBoardState().getShadowCardsInPlay(endState.getBoardState().getCurrentShadowPlayer()).stream()
                .filter(botCard -> CardType.MINION.equals(botCard.getSelf().getBlueprint().getCardType()))
                .mapToInt(botCard -> endState.getBoardState().getVitality(botCard)).sum();
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
        if (printDebugMessages) {
            System.out.println("Shadow phase plan asked to take action on " + awaitingDecision.toJson().toString());
        }

        if (!isActive()) {
            if (printDebugMessages) {
                System.out.println("Plan is outdated");
            }
            throw new IllegalStateException("Plan is outdated");
        }

        if (actions.isEmpty()) {
            if (printDebugMessages) {
                System.out.println("No actions in plan");
            }
            throw new IllegalStateException("No actions in plan");
        }

        if (nextStep > actions.size()) {
            if (printDebugMessages) {
                System.out.println("All actions from plan already fully taken");
            }
            throw new IllegalStateException("All actions from plan already fully taken");
        }

        ActionToTake action = actions.get(nextStep - 1);
        if (printDebugMessages) {
            System.out.println("Last action");
            System.out.println(action.toString());
        }

        if (action instanceof PlayCardFromHandWithTargetAction actionWithTarget) {
            if (printDebugMessages) {
                System.out.println("Target chosen by plan: " + actionWithTarget.getTarget().getBlueprint().getFullName());
            }
            return List.of(actionWithTarget.getTarget());
        } else if (action instanceof UseCardWithTargetAction actionWithTarget) {
            List<Integer> physicalIds = new ArrayList<>();
            if (awaitingDecision.getDecisionType().equals(AwaitingDecisionType.ARBITRARY_CARDS)) {
                for (String physicalCard : awaitingDecision.getDecisionParameters().get("physicalId")) {
                    physicalIds.add(Integer.parseInt(physicalCard));
                }

            } else if (awaitingDecision.getDecisionType().equals(AwaitingDecisionType.CARD_SELECTION)) {
                for (String physicalCard : awaitingDecision.getDecisionParameters().get("cardId")) {
                    physicalIds.add(Integer.parseInt(physicalCard));
                }
            }
            if (printDebugMessages) {
                System.out.println("Target chosen by plan: " + actionWithTarget.getTarget(physicalIds).getBlueprint().getFullName());
            }
            return List.of(actionWithTarget.getTarget(physicalIds));
        } else {
            throw new IllegalStateException("Last action should not trigger targeting");
        }
    }

    public boolean replanningNeeded() {
        return !isActive();
    }

    private boolean isActive() {
        boolean tbr =  !game.getGameState().getCurrentPlayerId().equals(playerName)
                && game.getGameState().getCurrentSiteNumber() == siteNumber
                && game.getGameState().getCurrentPhase().equals(Phase.SHADOW);
        if (printDebugMessages) {
            System.out.println("Plan is active: " + tbr);
        }
        return tbr;
    }
}
