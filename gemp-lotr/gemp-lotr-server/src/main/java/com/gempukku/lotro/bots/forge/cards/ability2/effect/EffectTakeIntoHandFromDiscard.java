package com.gempukku.lotro.bots.forge.cards.ability2.effect;

import com.gempukku.lotro.bots.forge.cards.ability2.util.HandValueUtil;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;
import com.gempukku.lotro.game.PhysicalCard;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class EffectTakeIntoHandFromDiscard extends EffectWithTarget {
    protected final Predicate<PhysicalCard> targetPredicate;

    public EffectTakeIntoHandFromDiscard(Predicate<PhysicalCard> targetPredicate) {
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
        return HandValueUtil.cardValueInHand(target, game);
    }

    @Override
    public String toString(String player, DefaultLotroGame game, List<PhysicalCard> targets) {
        if (targets.isEmpty()) {
            return "attempt to take card from discard, but none can be chosen";
        } else {
            String joined = targets.stream()
                    .map(t -> t.getBlueprint().getFullName())
                    .collect(Collectors.joining("; "));
            return  "take into hand card(s) from discard: " + joined;
        }
    }
}
