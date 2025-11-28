package com.gempukku.lotro.bots.forge.cards.ability2.effect;

import com.gempukku.lotro.bots.forge.cards.BotCardFactory;
import com.gempukku.lotro.bots.forge.cards.abstractcard.BotCard;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class EffectPlayWithBonus extends EffectWithTarget {
    protected final Predicate<PhysicalCard> targetPredicate;

    public EffectPlayWithBonus(Predicate<PhysicalCard> targetPredicate) {
        this.targetPredicate = targetPredicate;
    }

    @Override
    public final boolean decisionTextMatches(String decisionText) {
        return decisionText.equals("Choose card to play from hand");
    }

    @Override
    protected final ArrayList<PhysicalCard> getPotentialTargets(String player, DefaultLotroGame game) {
        return new ArrayList<>(game.getGameState().getHand(player).stream()
                .filter(targetPredicate)
                .map((Function<PhysicalCard, BotCard>) BotCardFactory::create)
                .filter(botCard -> botCard.canBePlayed(game))
                .map(BotCard::getSelf)
                .toList());
    }

    @Override
    protected final boolean affectsAll() {
        return false;
    }
}
