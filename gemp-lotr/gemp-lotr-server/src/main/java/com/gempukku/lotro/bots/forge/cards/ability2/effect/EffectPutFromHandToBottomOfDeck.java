package com.gempukku.lotro.bots.forge.cards.ability2.effect;

import com.gempukku.lotro.bots.forge.cards.ability2.util.HandValueUtil;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;
import com.gempukku.lotro.game.PhysicalCard;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class EffectPutFromHandToBottomOfDeck extends EffectWithTarget {
    protected final Predicate<PhysicalCard> targetPredicate;

    public EffectPutFromHandToBottomOfDeck(Predicate<PhysicalCard> targetPredicate) {
        this.targetPredicate = targetPredicate;
    }

    @Override
    public boolean decisionTextMatches(String decisionText) {
        return decisionText.equals("Choose cards from hand");
    }

    @Override
    protected ArrayList<PhysicalCard> getPotentialTargets(String player, DefaultLotroGame game) {
        return new ArrayList<>(game.getGameState().getHand(player).stream().filter(targetPredicate).toList());
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
        double targetValue = HandValueUtil.cardValueInHand(target, game);
        if (player.equals(target.getOwner())){
            if (targetValue > 0) {
                return -targetValue;
            } else {
                return 1;
            }
        } else {
            if (targetValue > 0) {
                return targetValue;
            } else {
                return -1;
            }
        }
    }

    @Override
    public String toString(String player, DefaultLotroGame game, List<PhysicalCard> targets) {
        if (targets.isEmpty()) {
            return "attempt to put cards from hand to the bottom of deck, but none can be chosen";
        } else {
            String joined = targets.stream()
                    .map(t -> t.getBlueprint().getFullName())
                    .collect(Collectors.joining("; "));
            return  "put card(s) from hand to the bottom of deck: " + joined;
        }
    }
}
