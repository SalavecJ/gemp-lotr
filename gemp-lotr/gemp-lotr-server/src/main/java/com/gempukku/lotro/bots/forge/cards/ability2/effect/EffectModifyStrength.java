package com.gempukku.lotro.bots.forge.cards.ability2.effect;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class EffectModifyStrength extends EffectWithTarget {
    protected final Predicate<PhysicalCard> targetPredicate;
    protected final int amount;

    public EffectModifyStrength(Predicate<PhysicalCard> targetPredicate, int amount) {
        this.targetPredicate = targetPredicate;
        this.amount = amount;
    }

    @Override
    public boolean decisionTextMatches(String decisionText) {
        throw new IllegalStateException("Not supported");
    }

    @Override
    protected ArrayList<PhysicalCard> getPotentialTargets(String player, DefaultLotroGame game) {
        return new ArrayList<>(game.getGameState().getActiveCards().stream()
                .filter(targetPredicate)
                .toList());
    }

    @Override
    protected boolean affectsAll() {
        return false;
    }

    @Override
    public String toString(String player, DefaultLotroGame game, List<PhysicalCard> targets) {
        if (targets.isEmpty()) {
            return "attempt to modify strength of a character, but none can be chosen";
        } else {
            String joined = targets.stream()
                    .map(t -> t.getBlueprint().getFullName())
                    .collect(Collectors.joining("; "));
            if (amount >= 0) {
                return "modify strength +" + amount + " of " + joined;
            } else {
                return "modify strength " + amount + " of " + joined;
            }
        }
    }

    @Override
    protected double getValueIfResolvedOn(String player, DefaultLotroGame game, PhysicalCard target) {
        boolean targetSkirmishing = game.getGameState().getSkirmish().getFellowshipCharacter().equals(target)
                || game.getGameState().getSkirmish().getShadowCharacters().contains(target);

        if (targetSkirmishing) {
            boolean correctSide = amount > 0 == target.getOwner().equals(player);
            if (correctSide) {
                return amount;
            } else {
                return -amount;
            }
        } else {
            return 0;
        }
    }
}
