package com.gempukku.lotro.bots.v2.fotr_starter.between_turns;

import com.gempukku.lotro.bots.v2.BotLogging;
import com.gempukku.lotro.bots.v2.decision.option.Option;
import com.gempukku.lotro.bots.v2.decision.option.SanctuaryHealOption;
import com.gempukku.lotro.bots.v2.decision.request.DecisionRequest;
import com.gempukku.lotro.bots.v2.fotr_starter.Plan;
import com.gempukku.lotro.bots.v2.fotr_starter.SimplePlan;
import com.gempukku.lotro.bots.v2.simulation.SimulatedGame;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class SanctuaryPlanner {

    public static Plan getPlan(DecisionRequest decisionRequest, DefaultLotroGame game, String player) {
        // last character from text prompt says how much can be healed
        int healAmount = Integer.parseInt(decisionRequest.getText().substring(decisionRequest.getText().length() - 1));

        Map<PhysicalCard, Integer> wounds = getCompanionsWoundsMap(game, player);
        int totalWounds = wounds.values().stream().mapToInt(Integer::intValue).sum();
        BotLogging.log(2, "Total wounds: " + totalWounds + ", heal amount: " + healAmount);

        if (totalWounds <= healAmount) {
            return getHealAllPlan(game, player);
        }

        wounds.forEach((physicalCard, woundCount) -> BotLogging.log(2, physicalCard.getBlueprint().getFullName() + "; " + woundCount + " wounds"));
        int woundsToStay = totalWounds - healAmount;
        BotLogging.log(2, "Wounds to stay: " + woundsToStay);

        Map<PhysicalCard, Integer> canHealLater = getCanHealLaterMap(game, player);
        canHealLater.forEach((physicalCard, integer) -> {
            if (integer > 0) {
                BotLogging.log(2, physicalCard.getBlueprint().getFullName() + "; can heal later: " + integer);
            }
        });

        return getHealPlan(game, player);
    }

    private static Map<PhysicalCard, Integer> getCompanionsWoundsMap(DefaultLotroGame game, String player) {
        Map<PhysicalCard, Integer> wounds = new HashMap<>();

        for (PhysicalCard physicalCard : game.getGameState().getInPlay()) {
            if (physicalCard.getOwner().equals(player) && physicalCard.getBlueprint().getCardType() == CardType.COMPANION) {
                if (game.getGameState().getWounds(physicalCard) > 0) {
                    wounds.put(physicalCard, game.getGameState().getWounds(physicalCard));
                }
            }
        }
        return wounds;
    }

    private static Map<PhysicalCard, Integer> getCanHealLaterMap(DefaultLotroGame game, String player) {
        Map<PhysicalCard, Integer> wounds = getCompanionsWoundsMap(game, player);

        Map<PhysicalCard, Integer> canHealLater = new HashMap<>();
        wounds.keySet().forEach(physicalCard -> canHealLater.put(physicalCard, 0));

        if (game.getGameState().getInPlay().stream().anyMatch((Predicate<PhysicalCard>) physicalCard ->
                physicalCard.getOwner().equals(player) && physicalCard.getBlueprint().getFullName().equals("Aragorn, King in Exile"))) {
            // if Aragorn is in play, he can heal Sam once for free
            canHealLater.forEach((physicalCard, integer) -> {
                if (physicalCard.getBlueprint().getFullName().equals("Sam, Son of Hamfast")) {
                    canHealLater.put(physicalCard, integer + 1);
                }
            });
        }

        for (PhysicalCard physicalCard : game.getGameState().getHand(player)) {
            // can discard companion with the same name to heal later
            if (physicalCard.getBlueprint().getCardType() == CardType.COMPANION) {
                canHealLater.forEach((companionInPlay, integer) -> {
                    if (companionInPlay.getBlueprint().getTitle().equals(physicalCard.getBlueprint().getTitle())) {
                        canHealLater.put(companionInPlay, integer + 1);
                    }
                });
            }
        }

        canHealLater.forEach((physicalCard, integer) -> {
            // check if wounded enough to heal later
            if (wounds.get(physicalCard) < integer) {
                canHealLater.put(physicalCard, wounds.get(physicalCard));
            }
        });
        return canHealLater;
    }

    private static Plan getHealPlan(DefaultLotroGame game, String player) {
        SimulatedGame simulatedGame = new SimulatedGame(game);
        List<List<Option>> actionsToTake = new ArrayList<>();
        DecisionRequest nextDecision = simulatedGame.getDecisionRequest(player);

        while (nextDecision != null && nextDecision.getOptions().stream().anyMatch(option -> option instanceof SanctuaryHealOption)) {
            DecisionRequest finalNextDecision = nextDecision;

            Map<PhysicalCard, Integer> wounds = getCompanionsWoundsMap(simulatedGame.getGame(), player);
            Map<PhysicalCard, Integer> canHealLater = getCanHealLaterMap(simulatedGame.getGame(), player);
            PhysicalCard toHeal = getCompanionToHeal(wounds, canHealLater);

            Option optionToTake = nextDecision.getOptions().stream()
                    .filter(option -> option instanceof SanctuaryHealOption sanctuaryHealOption && sanctuaryHealOption.getTarget().equals(toHeal))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No SanctuaryHealOption found for companion " + toHeal.getBlueprint().getFullName() + " in decision request: " + finalNextDecision));

            actionsToTake.add(List.of(optionToTake));
            simulatedGame.makeDecision(player, optionToTake.getAnswerForGame());
            nextDecision = simulatedGame.getDecisionRequest(player);
        }

        return new SimplePlan(actionsToTake);
    }

    private static PhysicalCard getCompanionToHeal(Map<PhysicalCard, Integer> wounds, Map<PhysicalCard, Integer> canHealLater) {
        List<String> priorityOrder = List.of(
                "Aragorn, King in Exile",
                "Gandalf, The Grey Wizard",
                "Boromir, Son of Denethor",
                "Legolas, Prince of Mirkwood",
                "Gimli, Dwarf of Erebor",
                "Sam, Son of Hamfast",
                "Frodo, Son of Drogo"
        );
        
        List<PhysicalCard> companions = new ArrayList<>(wounds.keySet());
        companions.removeIf(physicalCard -> {
            int woundCount = wounds.get(physicalCard);
            int canHealLaterCount = canHealLater.getOrDefault(physicalCard, 0);
            return woundCount <= canHealLaterCount;
        });

        if (companions.isEmpty()) {
            // Heal something that can be healed later
            companions = new ArrayList<>(wounds.keySet());
            companions.removeIf(physicalCard -> wounds.get(physicalCard) == 0);
        }
        
        companions.sort((c1, c2) -> {
            int index1 = priorityOrder.indexOf(c1.getBlueprint().getFullName());
            int index2 = priorityOrder.indexOf(c2.getBlueprint().getFullName());
            return Integer.compare(index1, index2);
        });
        
        return companions.getFirst();
    }

    private static Plan getHealAllPlan(DefaultLotroGame game, String player) {
        SimulatedGame simulatedGame = new SimulatedGame(game);
        List<List<Option>> actionsToTake = new ArrayList<>();
        DecisionRequest nextDecision = simulatedGame.getDecisionRequest(player);

        while (nextDecision != null && nextDecision.isSanctuaryHealDecision()) {
            DecisionRequest finalNextDecision = nextDecision;
            Option optionToTake = nextDecision.getOptions().stream()
                    .filter(option -> option instanceof SanctuaryHealOption)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No SanctuaryHealOption found in decision request: " + finalNextDecision));
            actionsToTake.add(List.of(optionToTake));
            simulatedGame.makeDecision(player, optionToTake.getAnswerForGame());
            nextDecision = simulatedGame.getDecisionRequest(player);
        }

        return new SimplePlan(actionsToTake);
    }
}
