package com.gempukku.lotro.bots.forge.cards.ability2.cost;

import com.gempukku.lotro.bots.forge.cards.ability2.util.WoundsValueUtil;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CostExert extends CostWithTarget {
    protected final Predicate<PhysicalCard> targetPredicate;
    protected final int amount;

    public CostExert(Predicate<PhysicalCard> targetPredicate, int amount) {
        this.targetPredicate = targetPredicate;
        this.amount = amount;
    }

    @Override
    public boolean decisionTextMatches(String decisionText) {
        return decisionText.equals("Choose cards to exert");
    }

    @Override
    public ArrayList<PhysicalCard> getPotentialTargets(String player, DefaultLotroGame game) {
        return new ArrayList<>(game.getGameState().getActiveCards().stream()
                .filter(card -> game.getModifiersQuerying().getVitality(game, card) > amount)
                .filter(targetPredicate)
                .toList());
    }

    @Override
    public String toString(String player, DefaultLotroGame game, List<PhysicalCard> targets ) {
        String joined = targets.stream()
                .map(t -> t.getBlueprint().getFullName())
                .collect(Collectors.joining("; "));
        if (amount == 1) {
            return "exert " + joined;
        } else {

            return "exert " + amount + " times " + joined;
        }
    }

    @Override
    public int getNumberOfTargetsRequired() {
        return 1;
    }

    @Override
    protected double getValueIfPayedWith(String player, DefaultLotroGame game, PhysicalCard target) {
        if (!canPayCostWithTarget(player, game, target)) {
            throw new IllegalStateException("Cost cannot be payed");
        }

        int vitality = game.getModifiersQuerying().getVitality(game, target);
        return WoundsValueUtil.evaluateWoundsChangeValue(player, game, target, Math.min(amount, vitality - 1));
    }
}
