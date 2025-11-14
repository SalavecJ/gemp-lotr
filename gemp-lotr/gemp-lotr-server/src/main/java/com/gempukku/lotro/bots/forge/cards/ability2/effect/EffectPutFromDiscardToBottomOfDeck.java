package com.gempukku.lotro.bots.forge.cards.ability2.effect;

import com.gempukku.lotro.bots.forge.cards.abstractcard.BotCard;
import com.gempukku.lotro.bots.forge.plan.PlannedBoardState;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Race;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class EffectPutFromDiscardToBottomOfDeck extends EffectWithTarget {
    protected final Predicate<BotCard> targetPredicate;

    public EffectPutFromDiscardToBottomOfDeck(Predicate<BotCard> targetPredicate) {
        this.targetPredicate = targetPredicate;
    }

    @Override
    public ArrayList<BotCard> getPotentialTargets(String player, PlannedBoardState plannedBoardState) {
        return new ArrayList<>(plannedBoardState.getDiscard(player).stream().filter(targetPredicate).toList());
    }

    @Override
    public boolean affectsAll() {
        return false;
    }

    @Override
    protected void resolveOn(String player, PlannedBoardState plannedBoardState, BotCard target) {
        plannedBoardState.moveFromDiscardToBottomOfDeck(target);
    }

    @Override
    protected double getValueIfResolvedOn(String player, PlannedBoardState plannedBoardState, BotCard target) {
        if (target == null) {
            return 0;
        }
        // Goblin Sneak logic
        if (target.getSelf().getBlueprint().getCulture() == Culture.MORIA
                && target.getSelf().getBlueprint().getRace() == Race.ORC) {
            int moriaOrcsInDiscard = (int) plannedBoardState.getDiscard(player).stream()
                    .filter(card -> card.getSelf().getBlueprint().getCulture() == Culture.MORIA
                            && card.getSelf().getBlueprint().getRace() == Race.ORC)
                    .count();
            if (moriaOrcsInDiscard <= 2) {
                return -5.0; // Keep orcs in discard for Host of Thousands
            }
            int sameNameInDiscard = (int) plannedBoardState.getDiscard(player).stream()
                    .filter(card -> card.getSelf().getBlueprint().getFullName().equals(target.getSelf().getBlueprint().getFullName()))
                    .count();
            if (sameNameInDiscard == 1) {
                return -5.0; // Keep last copy in discard for Host of Thousands variety
            }
        }

        return 0.1; // Hard to evaluate, give a small positive value
    }

    @Override
    public String toString(String player, PlannedBoardState plannedBoardState, List<BotCard> targets) {
        if (targets.isEmpty()) {
            return "attempt to put cards from discard to the bottom of deck, but none can be chosen";
        } else {
            String joined = targets.stream()
                    .map(t -> t.getSelf().getBlueprint().getFullName())
                    .collect(Collectors.joining("; "));
            return  "put card(s) from discard to the bottom of deck: " + joined;
        }
    }
}
