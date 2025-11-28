package com.gempukku.lotro.bots.forge.cards.ability2.effect;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class EffectPlayMinionFromDiscard extends EffectWithTarget {
    private final Predicate<PhysicalCard> targetPredicate;

    public EffectPlayMinionFromDiscard(Predicate<PhysicalCard> targetPredicate) {
        this.targetPredicate = targetPredicate;
    }

    @Override
    public boolean decisionTextMatches(String decisionText) {
        return decisionText.equals("Choose card from discard");
    }

    @Override
    protected ArrayList<PhysicalCard> getPotentialTargets(String player, DefaultLotroGame game) {
        throw new IllegalStateException("EffectPlayMinionFromDiscard does not support target selection, hard to calculate twilight");
    }

    @Override
    protected boolean affectsAll() {
        return false;
    }


    @Override
    protected double getValueIfResolvedOn(String player, DefaultLotroGame game, PhysicalCard target) {
        return 0.1; // Playing a card from discard has some value, but hard to quantify
    }

    @Override
    public String toString(String player, DefaultLotroGame game, List<PhysicalCard> targets) {
        if (targets.isEmpty()) {
            return "attempt to play card from discard, but none can be chosen";
        } else if (targets.size() == 1) {
            return "play " + targets.getFirst().getBlueprint().getFullName() + " from discard";
        } else {
            throw new IllegalStateException("EffectFromDiscard cannot be applied to multiple targets");
        }
    }
}
