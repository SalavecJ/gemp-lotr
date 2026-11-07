package com.gempukku.lotro.bots.v2.fotr_starter.archery;

import com.gempukku.lotro.bots.v2.BotLogging;
import com.gempukku.lotro.bots.v2.decision.option.ArcheryWoundOption;
import com.gempukku.lotro.bots.v2.decision.option.Option;
import com.gempukku.lotro.bots.v2.decision.option.PassOption;
import com.gempukku.lotro.bots.v2.decision.request.DecisionRequest;
import com.gempukku.lotro.bots.v2.fotr_starter.Plan;
import com.gempukku.lotro.bots.v2.fotr_starter.SimplePlan;
import com.gempukku.lotro.bots.v2.simulation.SimulatedGame;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.gempukku.lotro.bots.v2.BotLogging.LogLevel.DEBUG;
import static com.gempukku.lotro.bots.v2.BotLogging.LogLevel.REASONING;
import static com.gempukku.lotro.bots.v2.fotr_starter.utility.CardPredicates.hasFullName;
import static com.gempukku.lotro.bots.v2.fotr_starter.utility.GameQueries.*;
import static com.gempukku.lotro.bots.v2.fotr_starter.utility.OptionFinders.*;

public class FpArcheryWoundsPlanner {
    private static final String FRODO = "Frodo, Son of Drogo";
    private static final String GIMLI = "Gimli, Dwarf of Erebor";
    private static final String GANDALF = "Gandalf, The Grey Wizard";
    private static final String LEGOLAS = "Legolas, Prince of Mirkwood";
    private static final String BOROMIR = "Boromir, Son of Denethor";
    private static final String SAM = "Sam, Son of Hamfast";
    private static final String ARAGORN = "Aragorn, King in Exile";
    private static final String INTIMIDATE = "Intimidate";


    public static Plan getPlan(DecisionRequest decisionRequest, DefaultLotroGame game, String player) {
        if (!decisionRequest.getOptions().stream().allMatch(option -> option instanceof ArcheryWoundOption)) {
            throw new IllegalArgumentException("Decision request must contain only ArcheryWoundOption options");
        }

        SimulatedGame simulation = new SimulatedGame(game);

        for (PhysicalCard companion : getCompanionsInPlay(simulation, player)) {
            BotLogging.log(REASONING, companion.getBlueprint().getFullName() + " - vitality: " + getVitality(simulation, companion));
        }


        AtomicBoolean killFlag = new AtomicBoolean(false);

        Plan plan = new SimplePlan(runArcheryLoop(simulation, player, (d, s, p) -> {
            List<Option> options = d.getOptions();

            // check if the decision is about playing Intimidate
            if (d.isOptionalResponsesDecision()) {
                if (killFlag.get()) {
                    BotLogging.log(REASONING, "Save companion with Intimidate");
                    killFlag.set(false);
                    return List.of(findPlay(options, hasFullName(INTIMIDATE)));
                } else {
                    BotLogging.log(REASONING, "Companion is not dying, do not play Intimidate");
                    return List.of(findOptionOfType(options, PassOption.class));
                }
            }

            List<PhysicalCard> companions = new ArrayList<>(getArcheryTargets(options));

            // if there is just one companion, wound it
            if (companions.size() == 1) {
                PhysicalCard companionToWound = companions.getFirst();
                Option optionToTake = findOptionForArcheryTarget(options, companionToWound);
                BotLogging.log(REASONING, "Only one companion to wound: " + companionToWound.getBlueprint().getFullName());
                return List.of(optionToTake);
            }

            // if all companions have 1 vitality, choose according to the kill order
            if (companions.stream().allMatch(companion -> getVitality(s, companion) == 1)) {
                List<String> killOrder = getKillOrder(s, p);
                companions.sort(Comparator.comparingInt(c -> killOrder.indexOf(c.getBlueprint().getFullName())));
                PhysicalCard companionToWound = companions.getFirst();
                Option optionToTake = findOptionForArcheryTarget(options, companionToWound);
                BotLogging.log(REASONING, "All companions are exhausted, choosing according to the kill order: " + companionToWound.getBlueprint().getFullName());

                killFlag.set(true);

                return List.of(optionToTake);
            }

            // do not kill companions
            companions.removeAll(companions.stream().filter(companion -> getVitality(s, companion) == 1).toList());

            // if Frodo has more than 1 vitality, wound him
            if (companions.stream().anyMatch(companion -> companion.getBlueprint().getFullName().equals(FRODO))) {
                PhysicalCard frodo = companions.stream().filter(companion -> companion.getBlueprint().getFullName().equals(FRODO)).findFirst().get();
                Option optionToTake = findOptionForArcheryTarget(options, frodo);
                BotLogging.log(REASONING, "Frodo has " + getVitality(s, frodo) + " vitality, wound him");

                return List.of(optionToTake);
            }

            // if any companion has vitality above the threshold, choose one from them
            int vitalitySaveThreshold = getVitalitySaveThreshold(s, p);
            if (companions.stream().anyMatch(companion -> getVitality(s, companion) > vitalitySaveThreshold)) {
                companions.removeAll(companions.stream().filter(companion -> getVitality(s, companion) <= vitalitySaveThreshold).toList());
                // sort by vitality, then by kill order
                companions.sort((c1, c2) -> {
                    int vitalityComparison = Integer.compare(getVitality(s, c2), getVitality(s, c1));
                    if (vitalityComparison != 0) {
                        return vitalityComparison;
                    }
                    List<String> killOrder = getKillOrder(s, p);
                    return Integer.compare(killOrder.indexOf(c1.getBlueprint().getFullName()), killOrder.indexOf(c2.getBlueprint().getFullName()));
                });
                PhysicalCard companionToWound = companions.getFirst();
                Option optionToTake = findOptionForArcheryTarget(options, companionToWound);
                BotLogging.log(REASONING, "Companion is save to wound: " + companionToWound.getBlueprint().getFullName());

                return List.of(optionToTake);
            }

            // all companions risk being killed if wounded
            // do not endanger additional companions if not needed
            if (companions.stream().anyMatch(companion -> getVitality(s, companion) < vitalitySaveThreshold)) {
                BotLogging.log(REASONING, "Do not wound companions with exactly " + vitalitySaveThreshold + " vitality, if it can be prevented");
                companions.removeAll(companions.stream().filter(companion -> getVitality(s, companion) == vitalitySaveThreshold).toList());
            }

            // choose according to the kill order
            companions.sort(Comparator.comparingInt(c -> getKillOrder(s, p).indexOf(c.getBlueprint().getFullName())));
            PhysicalCard companionToWound = companions.getFirst();
            Option optionToTake = findOptionForArcheryTarget(options, companionToWound);
            BotLogging.log(REASONING, "Wounding companion and endangering them for the upcoming skirmishes: " + companionToWound.getBlueprint().getFullName());

            return List.of(optionToTake);
        }));


        BotLogging.log(DEBUG, "Archery wounds plan:\n" + plan);
        return plan;
    }

    private static List<String> getKillOrder(SimulatedGame simulation, String player) {
        return switch (getDeck(simulation, player)) {
            case ARAGORN_STARTER -> List.of(LEGOLAS, SAM, BOROMIR, ARAGORN, FRODO);
            case GANDALF_STARTER -> List.of(GIMLI, LEGOLAS, BOROMIR, GANDALF, FRODO);
        };
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
