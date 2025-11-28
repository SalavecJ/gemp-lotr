package com.gempukku.lotro.bots.forge.cards.ability2.effect;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.List;
import java.util.function.Predicate;

public class EffectPlayWithBonusDraw extends EffectPlayWithBonus {

    public EffectPlayWithBonusDraw(Predicate<PhysicalCard> targetPredicate) {
        super(targetPredicate);
    }

    @Override
    protected double getValueIfResolvedOn(String player, DefaultLotroGame game, PhysicalCard target) {
        if (game.getModifiersQuerying().canDrawCardAndIncrementForRuleOfFour(game, player)) {
            return 0.6;
        } else {
            return 0.0;
        }
    }

    @Override
    public String toString(String player, DefaultLotroGame game, List<PhysicalCard> targets) {
        if (targets.isEmpty()) {
            return "attempt to play card from hand with bonus draw, but none can be chosen";
        } else if (targets.size() == 1) {
            if (game.getModifiersQuerying().canDrawCardAndIncrementForRuleOfFour(game, player)) {
                return "play " + targets.getFirst().getBlueprint().getFullName() + " from hand to draw a card: " + game.getGameState().getDeck(player).getFirst().getBlueprint().getFullName();
            } else {
                return "play " + targets.getFirst().getBlueprint().getFullName() + " from hand to draw a card, but Rule of Four limit reached";
            }
        } else {
            throw new IllegalStateException("EffectPlayWithBonusDraw cannot be applied to multiple targets");
        }
    }
}
