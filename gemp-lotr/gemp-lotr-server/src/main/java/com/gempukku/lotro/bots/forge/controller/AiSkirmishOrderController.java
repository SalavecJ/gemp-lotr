package com.gempukku.lotro.bots.forge.controller;

import com.gempukku.lotro.bots.forge.utils.CombatUtil;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.Assignment;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.effects.ResolveSkirmishEffect;

import java.util.*;

public class AiSkirmishOrderController {
    private static final Set<String> RESOLVE_LATER = new HashSet<>(List.of(
            "1_97", // Boromir, Son of Denethod
            "1_145" // Uruk Brood
    ));
    private static final Set<String> RESOLVE_EARLIER = new HashSet<>(List.of(

    ));

    private final boolean printDebugMessages;
    private final LotroGame game;

    private final Map<PhysicalCard, List<PhysicalCard>> options;

    public AiSkirmishOrderController(boolean printDebugMessages, LotroGame game, List<PhysicalCard> options) {
        this.printDebugMessages = printDebugMessages;
        this.game = game;
        this.options = new HashMap<>();
        for (PhysicalCard fp : options) {
            this.options.put(fp, new ArrayList<>(game.getGameState().getAssignments().stream().filter(assignment -> assignment.getFellowshipCharacter().equals(fp)).map(Assignment::getShadowCharacters).toList().getFirst()));
        }
    }

    public PhysicalCard chooseNextSkirmish() {
        if (printDebugMessages) {
            System.out.println("Choosing next skirmish");
            System.out.println("Options: ");
            for (Map.Entry<PhysicalCard, List<PhysicalCard>> option : options.entrySet()) {
                StringBuilder builder = new StringBuilder(option.getKey().getBlueprint().getFullName());
                builder.append(" vs ");
                for (PhysicalCard minion : option.getValue()) {
                    builder.append(minion.getBlueprint().getFullName()).append("; ");
                }
                System.out.println(builder);
            }
        }

        // TODO something with threats

        PhysicalCard chosen = options.entrySet().stream().max(Comparator.comparingInt(assignment -> {
            PhysicalCard fp = assignment.getKey();
            List<PhysicalCard> minions = assignment.getValue();
            int score = 0;

            // Resolver bearer early to be sure we can use events later
            if (game.getGameState().getRingBearers().contains(fp)) {
                score++;
            }

            // Resolve specific cards early
            if (RESOLVE_EARLIER.contains(fp.getBlueprintId())) {
                score += 2;
            }
            for (PhysicalCard minion : minions) {
                if (RESOLVE_EARLIER.contains(minion.getBlueprintId())) {
                    score += 2;
                }
            }

            // Resolve specific cards later
            if (RESOLVE_LATER.contains(fp.getBlueprintId())) {
                score -= 2;
            }
            for (PhysicalCard minion : minions) {
                if (RESOLVE_LATER.contains(minion.getBlueprintId())) {
                    score -= 2;
                }
            }

            // Resolve winning fights first
            ResolveSkirmishEffect.Result potentialResult = CombatUtil.getPotentialResultIfNothingUsed(game, fp, new HashSet<>(minions));
            if (potentialResult.equals(ResolveSkirmishEffect.Result.SHADOW_LOSES) || potentialResult.equals(ResolveSkirmishEffect.Result.SHADOW_OVERWHELMED)) {
                score++;
            }

            return score;
        })).map(Map.Entry::getKey).orElseThrow();

        if (printDebugMessages) {
            System.out.println("Chosen: ");
            StringBuilder builder = new StringBuilder(chosen.getBlueprint().getFullName());
            builder.append(" vs ");
            for (PhysicalCard minion : options.get(chosen)) {
                builder.append(minion.getBlueprint().getFullName()).append("; ");
            }
            System.out.println(builder);

            if (RESOLVE_EARLIER.contains(chosen.getBlueprintId())) {
                System.out.println("Want to resolver skirmish early because of " + chosen.getBlueprint().getFullName());
            }
            for (PhysicalCard minion : options.get(chosen)) {
                if (RESOLVE_EARLIER.contains(minion.getBlueprintId())) {
                    System.out.println("Want to resolver skirmish early because of " + minion.getBlueprint().getFullName());
                }
            }

            if (RESOLVE_LATER.contains(chosen.getBlueprintId())) {
                System.out.println("Want to resolver skirmish late because of " + chosen.getBlueprint().getFullName());
            }
            for (PhysicalCard minion : options.get(chosen)) {
                if (RESOLVE_LATER.contains(minion.getBlueprintId())) {
                    System.out.println("Want to resolver skirmish late because of " + minion.getBlueprint().getFullName());
                }
            }

            System.out.println("Result if nothing gets played: " + CombatUtil.getPotentialResultIfNothingUsed(game, chosen, new HashSet<>(options.get(chosen))));
        }

        return chosen;
    }
}
