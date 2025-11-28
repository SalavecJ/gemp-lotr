package com.gempukku.lotro.bots.forge.cards.ability2.cost;

import com.gempukku.lotro.bots.forge.cards.ability2.util.HandValueUtil;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CostDiscardCardFromHand extends CostWithTarget {
    protected final Predicate<PhysicalCard> targetPredicate;
    private final int howMany;

    public CostDiscardCardFromHand(Predicate<PhysicalCard> targetPredicate, int howMany) {
        this.targetPredicate = targetPredicate;
        this.howMany = howMany;
    }

    @Override
    public ArrayList<PhysicalCard> getPotentialTargets(String player, DefaultLotroGame game) {
        return new ArrayList<>(game.getGameState().getHand(player).stream().filter(targetPredicate).toList());
    }

    @Override
    public String toString(String player, DefaultLotroGame game, List<PhysicalCard> targets) {
        String joined = targets.stream()
                .map(t -> t.getBlueprint().getFullName())
                .collect(Collectors.joining("; "));
        return "discard card(s) from hand: " + joined;
    }

    @Override
    public int getNumberOfTargetsRequired() {
        return howMany;
    }

    @Override
    public boolean decisionTextMatches(String decisionText) {
        return decisionText.equals("Choose cards from hand to discard");
    }

    @Override
    protected double getValueIfPayedWith(String player, DefaultLotroGame game, PhysicalCard target) {
        if (!canPayCostWithTarget(player, game, target)) {
            throw new IllegalStateException("Cost cannot be payed");
        }
        return getValueOfTarget(target, game);
    }

    public double getValueOfTarget(PhysicalCard target, DefaultLotroGame game) {
        if (target == null) {
            return 0;
        }
        double targetValue = HandValueUtil.cardValueInHand(target, game);
        if (targetValue > 0) {
            return -targetValue;
        } else {
            return 0.5;
        }
    }
}
