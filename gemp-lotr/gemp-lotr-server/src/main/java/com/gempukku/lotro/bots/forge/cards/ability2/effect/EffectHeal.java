package com.gempukku.lotro.bots.forge.cards.ability2.effect;

import com.gempukku.lotro.bots.forge.cards.ability2.util.WoundsValueUtil;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class EffectHeal extends EffectWithTarget {
    protected final Predicate<PhysicalCard> targetPredicate;
    protected final int amount;

    public EffectHeal(Predicate<PhysicalCard> targetPredicate, int amount) {
        this.targetPredicate = targetPredicate;
        this.amount = amount;
    }

    @Override
    public boolean decisionTextMatches(String decisionText) {
        return decisionText.equals("Choose cards to heal");
    }

    @Override
    protected ArrayList<PhysicalCard> getPotentialTargets(String player, DefaultLotroGame game) {
        return new ArrayList<>(game.getGameState().getActiveCards().stream()
                .filter(targetPredicate)
                .filter(card -> game.getGameState().getWounds(card) > 0)
                .toList());
    }

    @Override
    protected boolean affectsAll() {
        return false;
    }

    @Override
    public String toString(String player, DefaultLotroGame game, List<PhysicalCard> targets) {
        if (targets.isEmpty()) {
            return "attempt to heal a character, but none can be chosen";
        } else {
            String joined = targets.stream()
                    .map(t -> t.getBlueprint().getFullName())
                    .collect(Collectors.joining("; "));
            if (amount == 1) {
                return "remove a wound from " + joined;
            } else {
                return "remove " + amount + "wounds from " + joined;
            }
        }
    }

    @Override
    protected double getValueIfResolvedOn(String player, DefaultLotroGame game, PhysicalCard target) {
        return WoundsValueUtil.evaluateWoundsChangeValue(player, game, target, -amount);
    }
}
