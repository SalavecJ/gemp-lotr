package com.gempukku.lotro.bots.v2.fotr_starter.maneuver;

import com.gempukku.lotro.bots.v2.BotLogging;
import com.gempukku.lotro.bots.v2.decision.option.Option;
import com.gempukku.lotro.bots.v2.decision.option.PassOption;
import com.gempukku.lotro.bots.v2.decision.request.DecisionRequest;
import com.gempukku.lotro.bots.v2.fotr_starter.Plan;
import com.gempukku.lotro.bots.v2.fotr_starter.SimplePlan;
import com.gempukku.lotro.bots.v2.simulation.SimulatedGame;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;
import org.apache.commons.lang3.NotImplementedException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.gempukku.lotro.bots.v2.BotLogging.LogLevel.DEBUG;
import static com.gempukku.lotro.bots.v2.BotLogging.LogLevel.REASONING;
import static com.gempukku.lotro.bots.v2.fotr_starter.utility.CardPredicates.*;
import static com.gempukku.lotro.bots.v2.fotr_starter.utility.GameQueries.*;
import static com.gempukku.lotro.bots.v2.fotr_starter.utility.OptionFinders.*;


public class ManeuverPhasePlanner {
    private static final String LEGOLAS = "Legolas, Prince of Mirkwood";
    private static final String URUK_HAI_ARMORY = "Uruk-hai Armory";
    private static final String ARAGORN = "Aragorn, King in Exile";

    private static final String EMYN_MUIL = "Emyn Muil";
    private static final String URUK_SHAMAN = "Uruk Shaman";
    private static final String EREGIONS_TRAILS = "Eregion's Trails";

    public static Plan getPlan(DecisionRequest decisionRequest, DefaultLotroGame game, String player) {
        PhysicalCard currentSite = game.getGameState().getCurrentSite();
        boolean fpPlayer = game.getGameState().getCurrentPlayerId().equals(player);
        BotLogging.log(DEBUG, "Maneuver phase " + (fpPlayer ? "FP" : "Shadow") + " planning for site " + currentSite.getBlueprint().getSiteNumber() + ": " + currentSite.getBlueprint().getFullName());

        List<Option> options = decisionRequest.getOptions();

        if (decisionRequest.isManeuverPhaseDecision()
                && options.stream().allMatch(option -> option instanceof PassOption)) {
            BotLogging.log(REASONING, "Nothing to be done");
            Plan passPlan = getPassPlan();
            BotLogging.log(DEBUG, "Maneuver phase plan: Pass\n" + passPlan);
            return passPlan;
        }

        SimulatedGame simulation = new SimulatedGame(game);
        if (decisionRequest.isManeuverPhaseDecision()
                && findActivate(options, hasFullName(EMYN_MUIL)) != null) {
            Plan emynMuilPlan = getEmynMuilPlan(simulation, player);

            if (emynMuilPlan == null) {
                Plan passPlan = getPassPlan();
                BotLogging.log(DEBUG, "Maneuver phase plan: Pass\n" + passPlan);
                return passPlan;
            }

            BotLogging.log(DEBUG, "Maneuver phase plan: Emyn Muil activation\n" + emynMuilPlan);
            return emynMuilPlan;
        }

        if (decisionRequest.isManeuverPhaseDecision()
                && findActivate(options, hasFullName(URUK_SHAMAN)) != null) {
            Plan urukShamanPlan = getUrukShamanPlan(simulation, player);

            if (urukShamanPlan == null) {
                Plan passPlan = getPassPlan();
                BotLogging.log(DEBUG, "Maneuver phase plan: Pass\n" + passPlan);
                return passPlan;
            }

            BotLogging.log(DEBUG, "Maneuver phase plan: Uruk Shaman activation\n" + urukShamanPlan);
            return urukShamanPlan;
        }

        if (decisionRequest.isManeuverPhaseDecision()
                && findPlay(options, hasFullName(EREGIONS_TRAILS)) != null) {
            Plan eregionsTrailsPlan = getEregionsTrailsPlan(simulation, player);

            if (eregionsTrailsPlan == null) {
                Plan passPlan = getPassPlan();
                BotLogging.log(DEBUG, "Maneuver phase plan: Pass\n" + passPlan);
                return passPlan;
            }

            BotLogging.log(DEBUG, "Maneuver phase plan: Play Eregion's Trails\n" + eregionsTrailsPlan);
            return eregionsTrailsPlan;
        }

        throw new NotImplementedException("Not implemented maneuver phase plan for decision " + decisionRequest);
    }

    private static SimplePlan getPassPlan() {
        return new SimplePlan(List.of(List.of(new PassOption())));
    }

    private static Plan getEregionsTrailsPlan(SimulatedGame simulation, String player) {
        int roamingMinionsInPlay = getMinionsInPlay(simulation, simulation.getGame().getGameState().getOpponent(player)).stream()
                .filter(isRoaming(simulation))
                .toList()
                .size();

        if (roamingMinionsInPlay <= 1) {
            if (roamingMinionsInPlay == 0) {
                BotLogging.log(REASONING, "No roaming minions in play, do not play Eregion's Trails");
            } else {
                BotLogging.log(REASONING, "Only one roaming minion in play, do not play Eregion's Trails");
            }
            return null;
        }

        int aragornsVitality = getVitality(simulation, ARAGORN, player);

        if (aragornsVitality <= getVitalitySaveThreshold(simulation, player)) {
            BotLogging.log(REASONING, "Aragorn's vitality is " + aragornsVitality + ", risk of dying, do not play Eregion's Trails");
            return null;
        }

        BotLogging.log(REASONING, roamingMinionsInPlay + " roaming minions in play, Aragorn's vitality is " + aragornsVitality);
        return new SimplePlan(List.of(List.of(findPlay(simulation.getDecisionRequest(player).getOptions(), hasFullName(EREGIONS_TRAILS))), List.of(new PassOption())));
    }

    private static Plan getUrukShamanPlan(SimulatedGame simulation, String player) {
        Map<PhysicalCard, Integer> minionVitalityMap = getMinionsInPlay(simulation, player).stream()
                .collect(Collectors.toMap(minion -> minion, minion -> simulation.getGame().getModifiersQuerying().getVitality(simulation.getGame(), minion)));
        boolean allMinionsExhausted = minionVitalityMap.values().stream().allMatch(vitality -> vitality == 1);
        boolean enemyLegolasInPlay = isInPlay(simulation, LEGOLAS, simulation.getGame().getGameState().getOpponent(player));
        boolean urukhaiArmoryInPlay = isInPlay(simulation, URUK_HAI_ARMORY, player);

        if(!enemyLegolasInPlay || urukhaiArmoryInPlay) {
            BotLogging.log(REASONING, "No FP archery - " + (enemyLegolasInPlay ? "Uruk-hai Armory in play" : "Legolas not in play"));
            BotLogging.log(REASONING, "Activating Uruk Shaman does not save any minions from dying to FP archery fire");
            return null;
        }

        if (allMinionsExhausted) {
            BotLogging.log(REASONING, "All minions are exhausted and enemy Legolas is in play");
            PhysicalCard toHeal = minionVitalityMap.entrySet().stream()
                    .filter(entry -> entry.getValue() == 1)
                    .filter(entry -> entry.getKey().getBlueprint().getFullName().equals(URUK_SHAMAN))
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No minions to heal"));
            final Boolean[] healed = {false};

            BotLogging.log(REASONING, "Heal Uruk in play to avoid dying to FP archery fire");

            return new SimplePlan(runManeuverLoop(simulation, player, (d, s, p) -> {
                List<Option> options = d.getOptions();

                Option shamanOption = findActivate(options, hasFullName(URUK_SHAMAN));
                if (shamanOption != null && !healed[0]) {
                    healed[0] = true;
                    return List.of(shamanOption);
                }
                Option chooseTargetToHeal = findTargetWithId(options, toHeal.getCardId());
                if (chooseTargetToHeal != null) {
                    return List.of(chooseTargetToHeal);
                }
                return List.of(findOptionOfType(options, PassOption.class));

            }));
        } else {
            BotLogging.log(REASONING, "Not all minions are exhausted");
            BotLogging.log(REASONING, "Activating Uruk Shaman does not save any minions from dying to FP archery fire");
            return null;
        }
    }

    private static Plan getEmynMuilPlan(SimulatedGame simulation, String player) {
        Map<PhysicalCard, Integer> minionVitalityMap = getMinionsInPlay(simulation, player).stream()
                .collect(Collectors.toMap(minion -> minion, minion -> simulation.getGame().getModifiersQuerying().getVitality(simulation.getGame(), minion)));
        int minionsWithTwoVitality = minionVitalityMap.values().stream().filter(vitality -> vitality == 2).toList().size();
        int minionsWithThreeVitality = minionVitalityMap.values().stream().filter(vitality -> vitality == 3).toList().size();
        int minionsWithVitalityToExert = minionsWithTwoVitality + minionsWithThreeVitality;
        boolean enemyLegolasInPlay = isInPlay(simulation, LEGOLAS, simulation.getGame().getGameState().getOpponent(player));

        BotLogging.log(REASONING, "Emyn Muil activation available. Exertable minions: " + minionsWithVitalityToExert);
        BotLogging.log(REASONING, enemyLegolasInPlay ? "Legolas is in play, need to leave one vitality for archery fire" : "No FP archery");
        if (enemyLegolasInPlay && minionsWithThreeVitality > 0) {
            BotLogging.log(REASONING, "Minion with 3 vitality available for archery fire");
        }

        boolean leaveOneMinionOut = enemyLegolasInPlay && minionsWithThreeVitality == 0;
        List<PhysicalCard> minionsToExert = new ArrayList<>(minionVitalityMap.keySet().stream()
                .filter(minion -> minionVitalityMap.get(minion)  >= 2)
                //sort by strength ascending
                .sorted(Comparator.comparingInt(minion -> simulation.getGame().getModifiersQuerying().getStrength(simulation.getGame(), minion)))
                .toList());
        if (leaveOneMinionOut) {
            // ignore the weakest minion to leave it for archery fire
            minionsToExert.removeFirst();
        }

        if (minionsToExert.isEmpty()) {
            BotLogging.log(REASONING, "Do not activate Emyn Muil because of FP archery fire");
            return null;
        }

        Plan emynMuilPlan = new SimplePlan(runManeuverLoop(simulation, player, (d, s, p) -> {
            List<Option> options = d.getOptions();
            if (minionsToExert.isEmpty()) {
                return List.of(findOptionOfType(options, PassOption.class));
            }

            Option emynMuilOption = findActivate(options, hasFullName(EMYN_MUIL));
            if (emynMuilOption != null) {
                return List.of(emynMuilOption);
            }
            Option chooseNextMinionOption = findTargetWithId(options, minionsToExert.getFirst().getCardId());
            if (chooseNextMinionOption != null) {
                minionsToExert.removeFirst();
                return List.of(chooseNextMinionOption);
            }
            return List.of(findOptionOfType(options, PassOption.class));
        }));

        int fierceMinionsInPlay = getMinionsInPlay(simulation, player).stream()
                .filter(isFierce(simulation))
                .toList()
                .size();

        BotLogging.log(REASONING, "Make " + fierceMinionsInPlay + " minions fierce by activating Emyn Muil");
        return emynMuilPlan;
    }

    private static Predicate<PhysicalCard> isFierce(SimulatedGame simulation) {
        return physicalCard -> simulation.getGame().getModifiersQuerying().hasKeyword(simulation.getGame(), physicalCard, Keyword.FIERCE);
    }

    private static Predicate<PhysicalCard> isRoaming(SimulatedGame simulation) {
        return physicalCard -> simulation.getGame().getModifiersQuerying().hasKeyword(simulation.getGame(), physicalCard, Keyword.ROAMING);
    }


    // Picks the options to take for a decision, returns null if there is nothing more to do in this stage
    @FunctionalInterface
    private interface Chooser {
        List<Option> choose(DecisionRequest decision, SimulatedGame simulation, String player);
    }

    private static List<List<Option>> runManeuverLoop(SimulatedGame simulation, String player, Chooser chooser) {
        List<List<Option>> actions = new ArrayList<>();
        DecisionRequest decision = getNextDecision(simulation, player, actions);
        while (decision != null
                && simulation.getGame().getGameState().getCurrentPhase() == Phase.MANEUVER) {
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
            decision = getNextDecision(simulation, player, actions);
        }
        return actions;
    }

    private static DecisionRequest getNextDecision(SimulatedGame simulation, String player, List<List<Option>> actions) {
        DecisionRequest nextDecision = simulation.getDecisionRequest(player);
        if (nextDecision == null) {
            // Opponent's decision, doesn't matter what they do
            String opponent = simulation.getGame().getGameState().getOpponent(player);
            DecisionRequest opponentDecision = simulation.getDecisionRequest(opponent);
            if (opponentDecision == null) {
                throw new IllegalStateException("No decision available for any player");
            }
            simulation.makeDecision(opponent, opponentDecision.getOptions().getFirst().getAnswerForGame());
            nextDecision = simulation.getDecisionRequest(player);
        }

        return nextDecision;
    }
}
