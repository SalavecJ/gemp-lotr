package com.gempukku.lotro.bots.forge.cards.ability2.effect;

import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;
import com.gempukku.lotro.game.PhysicalCard;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class EffectPutFromDiscardToBottomOfDeck extends EffectWithTarget {
    protected final Predicate<PhysicalCard> targetPredicate;

    public EffectPutFromDiscardToBottomOfDeck(Predicate<PhysicalCard> targetPredicate) {
        this.targetPredicate = targetPredicate;
    }

    @Override
    public boolean decisionTextMatches(String decisionText) {
        return decisionText.equals("Choose card from discard");
    }

    @Override
    protected ArrayList<PhysicalCard> getPotentialTargets(String player, DefaultLotroGame game) {
        return new ArrayList<>(game.getGameState().getDiscard(player).stream().filter(targetPredicate).toList());
    }

    @Override
    protected boolean affectsAll() {
        return false;
    }

    @Override
    protected double getValueIfResolvedOn(String player, DefaultLotroGame game, PhysicalCard target) {
        if (target == null) {
            return 0;
        }
        // Goblin Sneak logic
        if (target.getBlueprint().getCulture() == Culture.MORIA
                && target.getBlueprint().getRace() == Race.ORC) {
            int moriaOrcsInDiscard = (int) game.getGameState().getDiscard(player).stream()
                    .filter(card -> card.getBlueprint().getCulture() == Culture.MORIA
                            && card.getBlueprint().getRace() == Race.ORC)
                    .count();
            if (moriaOrcsInDiscard <= 2) {
                return -5.0; // Keep orcs in discard for Host of Thousands
            }
            int sameNameInDiscard = (int) game.getGameState().getDiscard(player).stream()
                    .filter(card -> card.getBlueprint().getFullName().equals(target.getBlueprint().getFullName()))
                    .count();
            if (sameNameInDiscard == 1) {
                return -5.0; // Keep last copy in discard for Host of Thousands variety
            }
        }

        return 0.1; // Hard to evaluate, give a small positive value
    }

    @Override
    public String toString(String player, DefaultLotroGame game, List<PhysicalCard> targets) {
        if (targets.isEmpty()) {
            return "attempt to put cards from discard to the bottom of deck, but none can be chosen";
        } else {
            String joined = targets.stream()
                    .map(t -> t.getBlueprint().getFullName())
                    .collect(Collectors.joining("; "));
            return  "put card(s) from discard to the bottom of deck: " + joined;
        }
    }
}
