package com.gempukku.lotro.bots.v2.fotr_starter.shadow;

import com.gempukku.lotro.bots.v2.BotLogging;
import com.gempukku.lotro.bots.v2.decision.option.ChooseTargetForCardOption;
import com.gempukku.lotro.bots.v2.decision.option.Option;
import com.gempukku.lotro.bots.v2.decision.option.PassOption;
import com.gempukku.lotro.bots.v2.decision.option.RequiredResponseOption;
import com.gempukku.lotro.bots.v2.decision.request.DecisionRequest;
import com.gempukku.lotro.bots.v2.fotr_starter.Plan;
import com.gempukku.lotro.bots.v2.fotr_starter.SimplePlan;
import com.gempukku.lotro.bots.v2.fotr_starter.utility.DeadCardEvaluator;
import com.gempukku.lotro.bots.v2.simulation.SimulatedGame;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.gempukku.lotro.bots.v2.fotr_starter.utility.CardPredicates.*;
import static com.gempukku.lotro.bots.v2.fotr_starter.utility.GameQueries.*;
import static com.gempukku.lotro.bots.v2.fotr_starter.utility.OptionFinders.*;
import static com.gempukku.lotro.bots.v2.BotLogging.LogLevel.*;

public class GandalfShadowPhasePlanner {
    private static final String GOBLIN_RUNNER = "Goblin Runner";
    private static final String GOBLIN_SNEAK = "Goblin Sneak";
    private static final String MORIA_SCOUT = "Moria Scout";
    private static final String GOBLIN_SCAVENGERS = "Goblin Scavengers";
    private static final String GOBLIN_MARKSMAN = "Goblin Marksman";
    private static final String GOBLIN_PATROL_TROOP = "Goblin Patrol Troop";
    private static final String HOST_OF_THOUSANDS = "Host of Thousands";
    private static final String THEY_ARE_COMING = "They Are Coming";
    private static final String DRUMS = "Drums in the Deep";
    private static final String GOBLIN_SCIMITAR = "Goblin Scimitar";
    private static final String GREAT_SHIELD = "Great Shield";
    private static final String LEGOLAS = "Legolas, Prince of Mirkwood";
    private static final String ARAGORN = "Aragorn, King in Exile";
    private static final String MITHRIL_MINE = "Mithril Mine";
    private static final String DIMRILL_DALE = "Dimrill Dale";
    private static final String SHORES_OF_NEN_HITHOEL = "Shores of Nen Hithoel";
    private static final int SHORES_ORC_COUNT = 5;
    private static final int DISCARD_COUNT = 3;


    public static Plan getPlan(DefaultLotroGame game, String player) {
        int ourSiteNumber = game.getGameState().getPlayerPosition(player);
        PhysicalCard currentSite = game.getGameState().getCurrentSite();
        boolean opponentCanMoveAgain = canMoveAgain(game);
        BotLogging.log(DEBUG, "Shadow phase planning for site " + currentSite.getBlueprint().getSiteNumber() + ": " + currentSite.getBlueprint().getFullName());

        // Try playing as many archer minions as possible, then as many other minions as possible
        SimulatedGame archerySimulation = new SimulatedGame(game);
        Plan archeryPlan = getArcheryPlan(archerySimulation, player);
        int minionArchers = getNumberOfCardsInPlay(archerySimulation, GOBLIN_MARKSMAN, player);
        int enemyShields = getNumberOfCardsInPlay(archerySimulation, GREAT_SHIELD, game.getGameState().getOpponent(player));
        int archeryTotal = minionArchers - enemyShields;
        int companionCount = getNumberOfCompanionsInPlay(archerySimulation, game.getGameState().getOpponent(player)); // not specific to archery simulation
        int companionVitality = getTotalCompanionVitality(archerySimulation, game.getGameState().getOpponent(player));
        int vitalityToSpare = companionVitality - companionCount;
        // Check if we can kill anything in the archery phase
        if (archeryTotal > vitalityToSpare) {
            BotLogging.log(REASONING, "Archery plan can kill something, archery total: " + archeryTotal + ", companion vitality to spare: " + vitalityToSpare);
            BotLogging.log(DEBUG, "Shadow phase plan: Archery\n" + archeryPlan);
            return archeryPlan;
        }
        BotLogging.log(REASONING, "Archery plan cannot kill anything during archery phase, archery total: " + archeryTotal + ", companion vitality to spare: " + vitalityToSpare);
        // Check for Shores stop with archers
        if (currentSite.getBlueprint().getFullName().equals(SHORES_OF_NEN_HITHOEL)
                && getNumberOfMinionsInPlay(archerySimulation, player) >= SHORES_ORC_COUNT
                && opponentCanMoveAgain
                && ourSiteNumber >= 7) {
            BotLogging.log(REASONING, "Shores stop with archers");
            BotLogging.log(DEBUG, "Shadow phase plan: Archery\n" + archeryPlan);
            return archeryPlan;
        }

        // Try playing as many minions as possible
        SimulatedGame swarmSimulation = new SimulatedGame(game);
        Plan swarmPlan = getSwarmPlan(swarmSimulation, player);
        int minionsMax = getNumberOfMinionsInPlay(swarmSimulation, player);
        // Swarm is worth it if we can outnumber the opponent's companions
        if (minionsMax > companionCount) {
            BotLogging.log(REASONING, "Swarm plan can outnumber companions, minions: " + minionsMax + ", companions: " + companionCount);
            BotLogging.log(DEBUG, "Shadow phase plan: Swarm\n" + swarmPlan);
            return swarmPlan;
        }
        BotLogging.log(REASONING, "Swarm plan cannot outnumber companions, minions: " + minionsMax + ", companions: " + companionCount);
        // Check for Shores stop
        if (currentSite.getBlueprint().getFullName().equals(SHORES_OF_NEN_HITHOEL)
                && minionsMax >= SHORES_ORC_COUNT
                && opponentCanMoveAgain
                && ourSiteNumber >= 7) {
            BotLogging.log(REASONING, "Shores stop with swarm");
            BotLogging.log(DEBUG, "Shadow phase plan: Swarm\n" + swarmPlan);
            return swarmPlan;
        }
        // Play as many minions as possible if opponent is at site 9
        if (currentSite.getBlueprint().getSiteNumber() == 9) {
            BotLogging.log(REASONING, "Opponent is at site 9, playing as many minions as possible");
            BotLogging.log(DEBUG, "Shadow phase plan: Swarm\n" + swarmPlan);
            return swarmPlan;
        }

        // Try making huge Goblin Patrol Troop for overwhelm, fill with other minions to deal damage
        SimulatedGame goblinPatrolSimulation = new SimulatedGame(game);
        Plan goblinPatrolPlan = getGoblinPatrolPlan(goblinPatrolSimulation, player);
        if (goblinPatrolPlan != null) {
            BotLogging.log(REASONING, "Can make Goblin Patrol Troop with Goblin Scimitar");
            if (isInPlay(goblinPatrolSimulation, ARAGORN, goblinPatrolSimulation.getGame().getGameState().getOpponent(player))) {
                if (isInHand(goblinPatrolSimulation, DRUMS, player)) {
                    BotLogging.log(REASONING, "Drums in the Deep in hand to boost Goblin Patrol Troop against Aragorn");
                    return goblinPatrolPlan;
                } else {
                    BotLogging.log(REASONING, "Goblin Patrol Troop with Goblin Scimitar is not enough against Aragorn without Drums in the Deep in hand");
                }
            } else {
                BotLogging.log(DEBUG, "Shadow phase plan: Big Goblin Patrol\n" + goblinPatrolPlan);
                return goblinPatrolPlan;
            }
        } else {
            BotLogging.log(REASONING, "Cannot make Goblin Patrol Troop with Goblin Scimitar");
        }

        // Setup for future turns
        Plan setupPlan = getSetupPlan(new SimulatedGame(game), player);
        BotLogging.log(REASONING, "No other plan worked well, setting up for future turns");
        BotLogging.log(DEBUG, "Shadow phase plan: Setup\n" + setupPlan);
        return setupPlan;
    }

    private static Plan getSetupPlan(SimulatedGame simulation, String player) {
        PlanBuilder plan = new PlanBuilder(simulation, player);

        // Setup, then play strong minions to deal damage
        List<String> minionOrder = getStrongMinionsOrder();
        plan.run((d, s, p) -> playOtherCardsThenMinionsFromHand(d, s, minionOrder));

        // Use Mithril Mine if able
        plan.run((d, s, p) -> playScimitarsWithMithrilMine(d, s));

        plan.pass();

        return plan.build();
    }

    private static Plan getGoblinPatrolPlan(SimulatedGame simulation, String player) {
        PlanBuilder plan = new PlanBuilder(simulation, player);

        // Play Goblin Patrol Troop
        plan.run((d, s, p) -> playCardFromHandOrWithHost(d, s, p, GOBLIN_PATROL_TROOP));

        // Play Goblin Patrol Troop with They Are Coming
        plan.run((d, s, p) -> playCardWithTheyAreComing(d, s, p, GOBLIN_PATROL_TROOP));

        PhysicalCard goblinPatrolTroop = simulation.getGame().getGameState().getInPlay().stream()
                .filter(hasFullName(GOBLIN_PATROL_TROOP))
                .filter(card -> card.getOwner().equals(player))
                .findFirst()
                .orElse(null);

        if (goblinPatrolTroop == null) {
            // If we can't get Goblin Patrol Troop into play, we can't make a plan
            return null;
        }

        // Stop trying to attach Goblin Scimitar once the Goblin Patrol Troop has one (could have one from previous site)
        BooleanSupplier patrolHasScimitar = () -> !simulation.getGame().getGameState().getAttachedCards(goblinPatrolTroop).isEmpty();

        // Play Goblin Scimitar
        plan.runUntil(patrolHasScimitar, (d, s, p) -> playCardFromHandOrWithHost(d, s, p, GOBLIN_SCIMITAR));

        // Goblin Scimitar could not be played from hand, try to play it with Goblin Scavengers
        // Play Goblin Scavengers
        plan.runUntil(patrolHasScimitar, (d, s, p) -> playCardFromHandOrWithHost(d, s, p, GOBLIN_SCAVENGERS));

        // Play Goblin Scavengers with They Are Coming
        plan.runUntil(patrolHasScimitar, (d, s, p) -> playCardWithTheyAreComing(d, s, p, GOBLIN_SCAVENGERS));

        // Attach Goblin Scimitar to Goblin Patrol Troop
        plan.runUntil(patrolHasScimitar, GandalfShadowPhasePlanner::attachGoblinScimitarToGoblinPatrolTroop);

        if (!patrolHasScimitar.getAsBoolean()) {
            // If we can't get Goblin Scimitar attached to Goblin Patrol Troop, we can't make a plan
            return null;
        }

        // Setup, then play strong minions to deal damage
        List<String> minionOrder = getStrongMinionsOrder();
        plan.run((d, s, p) -> playOtherCardsThenMinionsFromHand(d, s, minionOrder));

        // Use Mithril Mine if able
        plan.run((d, s, p) -> playScimitarsWithMithrilMine(d, s));

        plan.pass();

        return plan.build();
    }

    private static Plan getSwarmPlan(SimulatedGame simulation, String player) {
        PlanBuilder plan = new PlanBuilder(simulation, player);

        // Get as many minions into play as possible
        List<String> minionOrder = getCheapestMinionOrder(simulation, player);
        plan.run((d, s, p) -> playMinionsThenOtherCardsFromHand(d, s, minionOrder));

        // Get as many minions into play with They Are Coming
        plan.run((d, s, p) -> playMinionsWithTheyAreComing(d, s, p, minionOrder));

        // Use Mithril Mine if able
        plan.run((d, s, p) -> playScimitarsWithMithrilMine(d, s));

        plan.pass();

        return plan.build();
    }

    private static Plan getArcheryPlan(SimulatedGame simulation, String player) {
        PlanBuilder plan = new PlanBuilder(simulation, player);

        // Get as many archers into play as possible by playing cards from hand
        plan.run((d, s, p) -> playCardFromHandOrWithHost(d, s, p, GOBLIN_MARKSMAN));

        // Get as many archers into play with They Are Coming
        plan.run((d, s, p) -> playCardWithTheyAreComing(d, s, p, GOBLIN_MARKSMAN));

        // Get as many minions into play as possible
        List<String> minionOrder = getCheapestMinionOrder(simulation, player);
        plan.run((d, s, p) -> playMinionsThenOtherCardsFromHand(d, s, minionOrder));

        // Get as many minions into play with They Are Coming
        plan.run((d, s, p) -> playMinionsWithTheyAreComing(d, s, p, minionOrder));

        // Use Mithril Mine if able
        plan.run((d, s, p) -> playScimitarsWithMithrilMine(d, s));

        plan.pass();

        return plan.build();
    }

    // Picks the options to take for a decision, returns null if there is nothing more to do in this stage
    @FunctionalInterface
    private interface Chooser {
        List<Option> choose(DecisionRequest decision, SimulatedGame simulation, String player);
    }

    // Plays the shadow phase in the simulation stage by stage, recording the actions taken
    private static final class PlanBuilder {
        private final SimulatedGame simulation;
        private final String player;
        private final List<List<Option>> actions = new ArrayList<>();

        private PlanBuilder(SimulatedGame simulation, String player) {
            this.simulation = simulation;
            this.player = player;
        }

        // Plays the shadow phase while the chooser returns options, stops when it returns null
        private void run(Chooser chooser) {
            runUntil(() -> false, chooser);
        }

        // Same as run, but also stops as soon as the stop condition is met
        private void runUntil(BooleanSupplier stopCondition, Chooser chooser) {
            DecisionRequest decision = getNextDecision(simulation, player, actions);
            while (decision != null
                    && simulation.getGame().getGameState().getCurrentPhase() == Phase.SHADOW
                    && !stopCondition.getAsBoolean()) {
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
        }

        // Ends the shadow phase, the plan has to end with the pass action
        private void pass() {
            DecisionRequest decision = getNextDecision(simulation, player, actions);

            if (!decision.isShadowPhaseDecision()) {
                throw new IllegalStateException("Not a shadow phase decision: " + decision);
            }
            Option pass = findOptionOfType(decision.getOptions(), PassOption.class);
            actions.add(List.of(pass));
            simulation.makeDecision(player, pass.getAnswerForGame());
        }

        private Plan build() {
            return new SimplePlan(actions);
        }
    }

    private static List<Option> playCardFromHandOrWithHost(DecisionRequest decision, SimulatedGame simulation, String player, String cardName) {
        List<Option> options = decision.getOptions();
        return asAction(firstAvailable(
                // Play the card
                () -> findPlay(options, hasFullName(cardName)),
                // Play Host of Thousands if it can play the card
                () -> canPlayCardWithHostOfThousands(simulation, player, cardName) ? findPlay(options, hasFullName(HOST_OF_THOUSANDS)) : null,
                // Choose the card as a target for Host of Thousands
                () -> findArbitraryTarget(options, cardName).orElse(null)
        )); // Null if the card cannot be played by cards from hand
    }

    private static List<Option> playCardWithTheyAreComing(DecisionRequest decision, SimulatedGame simulation, String player, String cardName) {
        if (decision.getMinToSelect() > 1) {
            return chooseCardsToDiscardForTheyAreComing(decision, simulation, player);
        }
        List<Option> options = decision.getOptions();
        return asAction(firstAvailable(
                // Activate They Are Coming if it can play the card next action
                () -> canPlayCardWithTheyAreComing(simulation, player, cardName) ? findActivate(options, hasFullName(THEY_ARE_COMING)) : null,
                // Play They Are Coming if it can play the card next action
                () -> canPlayTheyAreComingIntoCard(simulation, player, cardName) ? findPlay(options, hasFullName(THEY_ARE_COMING)) : null,
                // Choose the card as a target for They Are Coming
                () -> findArbitraryTarget(options, cardName).orElse(null)
        )); // Null if the card cannot be played by They Are Coming
    }
    private static List<Option> playMinionsThenOtherCardsFromHand(DecisionRequest decision, SimulatedGame simulation, List<String> minionOrder) {
        List<Option> options = decision.getOptions();
        return asAction(firstAvailable(
                // Play minions from hand
                () -> findFirstPlay(options, minionOrder),
                // Play Host of Thousands
                () -> findPlay(options, hasFullName(HOST_OF_THOUSANDS)),
                // Choose a minion to play with Host of Thousands
                () -> isArbitraryTargetChoiceFor(decision, HOST_OF_THOUSANDS) ? findFirstTarget(options, minionOrder) : null,
                // Play Goblin Scimitar
                () -> findPlay(options, hasFullName(GOBLIN_SCIMITAR)),
                // Play They Are Coming
                () -> findPlay(options, hasFullName(THEY_ARE_COMING)),
                // If Goblin Scimitar or Goblin Scavengers get played
                () -> attachGoblinScimitarToWeakestTarget(decision, simulation)
        )); // Null if there are no more cards in hand to play
    }

    private static List<Option> playOtherCardsThenMinionsFromHand(DecisionRequest decision, SimulatedGame simulation, List<String> minionOrder) {
        List<Option> options = decision.getOptions();
        return asAction(firstAvailable(
                // Play Goblin Scimitar
                () -> findPlay(options, hasFullName(GOBLIN_SCIMITAR)),
                // Play They Are Coming
                () -> findPlay(options, hasFullName(THEY_ARE_COMING)),
                // Play minions from hand
                () -> findFirstPlay(options, minionOrder),
                // Play Host of Thousands
                () -> findPlay(options, hasFullName(HOST_OF_THOUSANDS)),
                // Choose a minion to play with Host of Thousands
                () -> isArbitraryTargetChoiceFor(decision, HOST_OF_THOUSANDS) ? findFirstTarget(options, minionOrder) : null,
                // If Goblin Scimitar or Goblin Scavengers get played
                () -> attachGoblinScimitarToWeakestTarget(decision, simulation)
        ));
    }

    private static List<Option> playMinionsWithTheyAreComing(DecisionRequest decision, SimulatedGame simulation, String player, List<String> minionOrder) {
        if (decision.getMinToSelect() > 1) {
            return chooseCardsToDiscardForTheyAreComing(decision, simulation, player);
        }
        List<Option> options = decision.getOptions();
        return asAction(firstAvailable(
                // Always activate They Are Coming
                () -> findActivate(options, hasFullName(THEY_ARE_COMING)),
                // Choose a minion to play with They Are Coming
                () -> isArbitraryTargetChoiceFor(decision, THEY_ARE_COMING) ? findFirstTarget(options, minionOrder) : null,
                // If Goblin Scavengers get played
                () -> attachGoblinScimitarToWeakestTarget(decision, simulation)
        )); // Null if there are no more minions to play with They Are Coming
    }

    private static List<Option> playScimitarsWithMithrilMine(DecisionRequest decision, SimulatedGame simulation) {
        List<Option> options = decision.getOptions();
        return asAction(firstAvailable(
                // Activate Mithril Mine
                () -> findActivate(options, hasFullName(MITHRIL_MINE)),
                // Attach Goblin Scimitar
                () -> attachGoblinScimitarToWeakestTarget(decision, simulation)
        ));
    }

    private static Option attachGoblinScimitarToWeakestTarget(DecisionRequest decision, SimulatedGame simulation) {
        List<Option> options = decision.getOptions();
        return firstAvailable(
                // Always attach Goblin Scimitar to the weakest minion in play
                () -> decision.isChooseTargetForCardDecision() && sourceHasFullName(decision, GOBLIN_SCIMITAR) ? chooseWeakestTarget(options, simulation) : null
        );
    }

    private static List<Option> attachGoblinScimitarToGoblinPatrolTroop(DecisionRequest decision, SimulatedGame simulation, String player) {
        List<Option> options = decision.getOptions();
        return asAction(firstAvailable(
                // Always attach Goblin Scimitar to Goblin Patrol Troop
                () -> decision.isChooseTargetForCardDecision() && sourceHasFullName(decision, GOBLIN_SCIMITAR) ? findTargetWithFullName(options, GOBLIN_PATROL_TROOP) : null
        ));
    }

    private static boolean canPlayCardWithHostOfThousands(SimulatedGame simulation, String player, String cardName) {
        SimulatedGame copy = simulation.copy();
        int originalCount = getNumberOfCardsInPlay(copy, cardName, player);
        DecisionRequest decision = getNextDecision(copy, player, null);

        // Play Host of Thousands
        Option playHostOfThousands = findPlay(decision.getOptions(), hasFullName(HOST_OF_THOUSANDS));
        if (playHostOfThousands == null) {
            return false;
        }
        copy.makeDecision(player, playHostOfThousands.getAnswerForGame());

        decision = getNextDecision(copy, player, null);
        return isCardAvailableAfterPlay(decision, copy, player, cardName, originalCount, isArbitraryTargetChoiceFor(decision, HOST_OF_THOUSANDS));
    }

    private static boolean canPlayTheyAreComingIntoCard(SimulatedGame simulation, String player, String cardName) {
        SimulatedGame copy = simulation.copy();
        DecisionRequest decision = getNextDecision(copy, player, null);

        // Play They Are Coming
        Option playTheyAreComing = findPlay(decision.getOptions(), hasFullName(THEY_ARE_COMING));
        if (playTheyAreComing == null) {
            return false;
        }
        copy.makeDecision(player, playTheyAreComing.getAnswerForGame());

        return canPlayCardWithTheyAreComing(copy, player, cardName);
    }

    private static boolean canPlayCardWithTheyAreComing(SimulatedGame simulation, String player, String cardName) {
        SimulatedGame copy = simulation.copy();
        int originalCount = getNumberOfCardsInPlay(copy, cardName, player);
        DecisionRequest decision = getNextDecision(copy, player, null);

        // Activate They Are Coming
        Option activateTheyAreComing = findActivate(decision.getOptions(), hasFullName(THEY_ARE_COMING));
        if (activateTheyAreComing == null) {
            return false;
        }
        copy.makeDecision(player, activateTheyAreComing.getAnswerForGame());

        decision = getNextDecision(copy, player, null);
        if (!decision.isShadowPhaseDecision() && !isArbitraryTargetChoiceFor(decision, THEY_ARE_COMING)) {
            // Choose 3 cards to discard
            List<Option> toDiscard = chooseCardsToDiscardForTheyAreComing(decision, copy, player);
            copy.makeDecision(player, decision.combineSelected(toDiscard));
            decision = getNextDecision(copy, player, null);
        }
        // Either the card got played without choosing a target, or we need to choose it as a target
        return isCardAvailableAfterPlay(decision, copy, player, cardName, originalCount, !decision.isShadowPhaseDecision());
    }

    // The card is available if it got played without choosing a target, or if it can be chosen as a target
    private static boolean isCardAvailableAfterPlay(DecisionRequest decision, SimulatedGame simulation, String player,
                                                    String cardName, int originalCount, boolean choosingTarget) {
        if (choosingTarget) {
            return findArbitraryTarget(decision.getOptions(), cardName).isPresent();
        }
        return getNumberOfCardsInPlay(simulation, cardName, player) > originalCount;
    }

    private static List<Option> chooseCardsToDiscardForTheyAreComing(DecisionRequest decision, SimulatedGame simulation, String player) {
        List<PhysicalCard> targets = new ArrayList<>(getTargets(decision.getOptions()));
        if (targets.size() < DISCARD_COUNT) {
            throw new IllegalStateException("Not enough cards to discard for They Are Coming: " + targets);
        }
        Set<Integer> idsToDiscard = new HashSet<>();
        while (idsToDiscard.size() < DISCARD_COUNT) {
            PhysicalCard cardToDiscard = chooseCardToDiscard(simulation, targets, player);
            idsToDiscard.add(cardToDiscard.getCardId());
            targets.remove(cardToDiscard);
        }
        return decision.getOptions().stream()
                .filter(option -> option instanceof ChooseTargetForCardOption chooseTarget && idsToDiscard.contains(chooseTarget.getTarget().getCardId()))
                .toList();
    }

    private static PhysicalCard chooseCardToDiscard(SimulatedGame simulation, List<PhysicalCard> targets, String player) {
        var game = simulation.getGame();
        // Preferences in order, the first one that matches any target wins
        List<Predicate<PhysicalCard>> preferences = List.of(
                // Cards that can never be played
                target -> DeadCardEvaluator.canNeverBePlayed(game, player, target),
                // Then cards that can never have value
                target -> DeadCardEvaluator.canNeverHaveValue(game, player, target),
                // Then another copy of They Are Coming since we have one in play already
                target -> target.getBlueprint().getFullName().equals(THEY_ARE_COMING),
                // Then FP event cards
                target -> target.getBlueprint().getSide() == Side.FREE_PEOPLE && target.getBlueprint().getCardType() == CardType.EVENT,
                // Then shadow cards
                target -> target.getBlueprint().getSide() == Side.SHADOW
        );
        for (Predicate<PhysicalCard> preference : preferences) {
            Optional<PhysicalCard> match = targets.stream().filter(preference).findFirst();
            if (match.isPresent()) {
                return match.get();
            }
        }
        // Then any card
        return targets.getFirst();
    }

    private static boolean isArbitraryTargetChoiceFor(DecisionRequest decision, String sourceFullName) {
        return decision.isChooseArbitraryTargetForCardDecision() && sourceHasFullName(decision, sourceFullName);
    }

    private static Option findFirstPlay(List<Option> options, List<String> fullNames) {
        for (String fullName : fullNames) {
            Option option = findPlay(options, hasFullName(fullName));
            if (option != null) {
                return option;
            }
        }
        return null;
    }

    private static Option chooseWeakestTarget(List<Option> options, SimulatedGame simulation) {
        return options.stream()
                .map(o -> (ChooseTargetForCardOption) o)
                .min(Comparator.comparingInt(o -> getStrength(simulation, o.getTarget())))
                .get();
    }

    private static int getStrength(SimulatedGame simulation, PhysicalCard target) {
        return simulation.getGame().getModifiersQuerying().getStrength(simulation.getGame(), target);
    }

    private static Option findFirstTarget(List<Option> options, List<String> minionOrder) {
        for (String minion : minionOrder) {
            Optional<Option> option = findArbitraryTarget(options, minion);
            if (option.isPresent()) {
                return option.get();
            }
        }
        return options.getFirst();
    }

    private static List<String> getCheapestMinionOrder(SimulatedGame simulation, String player) {
        boolean opponentHasLegolas = isInPlay(simulation, LEGOLAS, simulation.getGame().getGameState().getOpponent(player));
        return opponentHasLegolas ?
                List.of(GOBLIN_RUNNER, MORIA_SCOUT, GOBLIN_SNEAK, GOBLIN_SCAVENGERS, GOBLIN_MARKSMAN, GOBLIN_PATROL_TROOP) :
                List.of(GOBLIN_RUNNER, GOBLIN_SNEAK, MORIA_SCOUT, GOBLIN_SCAVENGERS, GOBLIN_MARKSMAN, GOBLIN_PATROL_TROOP);
    }

    private static List<String> getStrongMinionsOrder() {
        // Do not play sneak or runner
        return List.of(GOBLIN_PATROL_TROOP, GOBLIN_MARKSMAN, GOBLIN_SCAVENGERS, MORIA_SCOUT);
    }

    // Returns the first option that is available, null if there is none. Rules are evaluated lazily, in order.
    @SafeVarargs
    private static Option firstAvailable(Supplier<Option>... rules) {
        for (Supplier<Option> rule : rules) {
            Option option = rule.get();
            if (option != null) {
                return option;
            }
        }
        return null;
    }

    private static List<Option> asAction(Option option) {
        return option == null ? null : List.of(option);
    }

    private static DecisionRequest getNextDecision(SimulatedGame simulation, String player, List<List<Option>> actions) {
        DecisionRequest nextDecision = simulation.getDecisionRequest(player);
        if (nextDecision == null) {
            // Dimrill Dale response for opponent
            String opponent = simulation.getGame().getGameState().getOpponent(player);
            DecisionRequest opponentDecision = simulation.getDecisionRequest(opponent);
            if (opponentDecision == null) {
                throw new IllegalStateException("No decision available for any player");
            }
            if (opponentDecision.getOptions().stream().anyMatch(option -> option instanceof RequiredResponseOption requiredResponseOption && requiredResponseOption.getSource().getBlueprint().getFullName().equals(DIMRILL_DALE))) {
                // Order of responses on Dimrill Dale does not matter, just pick the first option for the opponent
                simulation.makeDecision(opponent, opponentDecision.getOptions().getFirst().getAnswerForGame());
                nextDecision = simulation.getDecisionRequest(player);
            }
        }

        // Automatic triggers and targeting
        while (nextDecision != null
                && simulation.getGame().getGameState().getCurrentPhase() == Phase.SHADOW) {
            List<Option> chosenOptions = chooseAutomaticDecision(nextDecision, simulation);
            if (chosenOptions == null) {
                break;
            }
            if (actions != null) {
                actions.add(chosenOptions);
            }
            simulation.makeDecision(player, chosenOptions.getFirst().getAnswerForGame());
            nextDecision = simulation.getDecisionRequest(player);
        }

        return nextDecision;
    }

    // Handles Goblin Sneak, Goblin Scimitar and Goblin Scavengers triggers, returns null if none of them applies
    // Automatically activate Shores of Nen Hithoel if opponent can move
    private static List<Option> chooseAutomaticDecision(DecisionRequest decision, SimulatedGame simulation) {
        List<Option> options = decision.getOptions();
        return asAction(firstAvailable(
                // Never activate Goblin Sneak
                () -> findActivate(options, hasFullName(GOBLIN_SNEAK)) != null ? findOptionOfType(options, PassOption.class) : null,
                // Always activate Goblin Scimitar
                () -> findActivate(options, hasFullName(GOBLIN_SCIMITAR)),
                // Always activate Goblin Scavengers
                () -> findActivate(options, hasFullName(GOBLIN_SCAVENGERS)),
                // Always choose Goblin Scimitar for Goblin Scavengers
                () -> isArbitraryTargetChoiceFor(decision, GOBLIN_SCAVENGERS) ? findArbitraryTarget(options, GOBLIN_SCIMITAR).get() : null,
                // Always choose Goblin Scimitar for Mithril Mine
                () -> isArbitraryTargetChoiceFor(decision, MITHRIL_MINE) ? findArbitraryTarget(options, GOBLIN_SCIMITAR).get() : null,
                // Always activate Shores of Nen Hithoel if opponent can move again
                () -> findActivate(options, hasFullName(SHORES_OF_NEN_HITHOEL)) != null && canMoveAgain(simulation.getGame()) ? findActivate(options, hasFullName(SHORES_OF_NEN_HITHOEL)) : null
        ));
    }

}