package com.gempukku.lotro.bots.v2.fotr_starter.archery;

import com.gempukku.lotro.bots.v2.BotLogging;
import com.gempukku.lotro.bots.v2.decision.option.ArcheryWoundOption;
import com.gempukku.lotro.bots.v2.decision.option.Option;
import com.gempukku.lotro.bots.v2.decision.request.DecisionRequest;
import com.gempukku.lotro.bots.v2.fotr_starter.Plan;
import com.gempukku.lotro.bots.v2.fotr_starter.SimplePlan;
import com.gempukku.lotro.bots.v2.simulation.SimulatedGame;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.*;

import static com.gempukku.lotro.bots.v2.BotLogging.LogLevel.DEBUG;
import static com.gempukku.lotro.bots.v2.BotLogging.LogLevel.REASONING;
import static com.gempukku.lotro.bots.v2.fotr_starter.utility.GameQueries.*;
import static com.gempukku.lotro.bots.v2.fotr_starter.utility.OptionFinders.*;

public class ShadowArcheryWoundsPlanner {
    public static Plan getPlan(DecisionRequest decisionRequest, DefaultLotroGame game, String player) {
        if (!decisionRequest.getOptions().stream().allMatch(option -> option instanceof ArcheryWoundOption)) {
            throw new IllegalArgumentException("Decision request must contain only ArcheryWoundOption options");
        }

        SimulatedGame simulation = new SimulatedGame(game);

        for (PhysicalCard minion : getMinionsInPlay(simulation, player)) {
            BotLogging.log(REASONING, minion.getBlueprint().getFullName() + " - vitality: " + getVitality(simulation, minion) + ", strength: " + getStrength(simulation, minion));
        }

        Plan plan = new SimplePlan(runArcheryLoop(simulation, player, (d, s, p) -> {
            List<Option> options = d.getOptions();
            List<PhysicalCard> minions = new ArrayList<>(getArcheryTargets(options));

            // if there is just one minion, wound it
            if (minions.size() == 1) {
                PhysicalCard minionToWound = minions.getFirst();
                Option optionToTake = findOptionForArcheryTarget(options, minionToWound);
                BotLogging.log(REASONING, "Only one minion to wound: " + minionToWound.getBlueprint().getFullName());
                return List.of(optionToTake);
            }

            // if all minions have 1 vitality, choose the one with the lowest strength to kill
            if (minions.stream().allMatch(minion -> getVitality(s, minion) == 1)) {
                minions.sort(Comparator.comparingInt(m -> getStrength(s, m)));
                PhysicalCard minionToWound = minions.getFirst();
                Option optionToTake = findOptionForArcheryTarget(options, minionToWound);
                BotLogging.log(REASONING, "All minions are exhausted, choosing the lowest strength: " + minionToWound.getBlueprint().getFullName());

                return List.of(optionToTake);
            }

            // choose minion to wound and survive
            // discard all exhausted minions
            minions.removeAll(minions.stream().filter(minion -> getVitality(s, minion) == 1).toList());
            // sort by vitality descending, then strength ascending

            minions.sort((m1, m2) -> {
                int vitalityComparison = Integer.compare(getVitality(s, m2), getVitality(s, m1));
                if (vitalityComparison != 0) {
                    return vitalityComparison;
                }
                return Integer.compare(getStrength(s, m1), getStrength(s, m2));
            });
            PhysicalCard minionToWound = minions.getFirst();
            Option optionToTake = findOptionForArcheryTarget(options, minionToWound);

            BotLogging.log(REASONING, "Choosing to wound minion with the highest vitality and the lowest strength: " + minionToWound.getBlueprint().getFullName());

            return List.of(optionToTake);
        }));

        BotLogging.log(DEBUG, "Archery wounds plan:\n" + plan);
        return plan;
    }


    // Picks the options to take for a decision, returns null if there is nothing more to do in this stage
    @FunctionalInterface
    private interface Chooser {
        List<Option> choose(DecisionRequest decision, SimulatedGame simulation, String player);
    }

    private static List<List<Option>> runArcheryLoop(SimulatedGame simulation, String player, Chooser chooser) {
        List<List<Option>> actions = new ArrayList<>();
        DecisionRequest decision = simulation.getDecisionRequest(player);
        while (decision != null
                && simulation.getGame().getGameState().getCurrentPhase() == Phase.ARCHERY) {
            List<Option> chosenOptions = chooser.choose(decision, simulation, player);
            if (chosenOptions == null) {
                break;
            }
            actions.add(chosenOptions);
            if (decision.getMinToSelect() > 1) {
                simulation.makeDecision(player, decision.combineSelected(chosenOptions));
            } else {
                simulation.makeDecision(player, chosenOptions.getFirst().getAnswerForGame());
            }
            decision = simulation.getDecisionRequest(player);
        }
        return actions;
    }
}
