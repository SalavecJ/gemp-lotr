package com.gempukku.lotro.bots.forge.cards.ability2.effect;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class EffectDiscardFromPlay extends EffectWithTarget {
    protected final Predicate<PhysicalCard> targetPredicate;
    protected final boolean discardAll;

    public EffectDiscardFromPlay(Predicate<PhysicalCard> targetPredicate, boolean discardAll) {
        this.targetPredicate = targetPredicate;
        this.discardAll = discardAll;
    }

    @Override
    public boolean decisionTextMatches(String decisionText) {
        System.out.println(decisionText);
        throw new IllegalStateException("Not implemented yet");
    }

    @Override
    protected ArrayList<PhysicalCard> getPotentialTargets(String player, DefaultLotroGame game) {
        return new ArrayList<>(game.getGameState().getActiveCards().stream().filter(targetPredicate).toList());
    }

    @Override
    protected boolean affectsAll() {
        return discardAll;
    }

    @Override
    public String toString(String player, DefaultLotroGame game, List<PhysicalCard> targets) {
        if (targets.isEmpty()) {
            return "discard cards from play, but none will be affected";
        } else {
            String joined = targets.stream()
                    .map(t -> t.getBlueprint().getFullName())
                    .collect(Collectors.joining("; "));
           return  "discard from play: " + joined;
        }
    }

    @Override
    protected double getValueIfResolvedOn(String player, DefaultLotroGame game, PhysicalCard target) {
        return target.getOwner().equals(player) ? -1.1: 1.1;
    }
}
