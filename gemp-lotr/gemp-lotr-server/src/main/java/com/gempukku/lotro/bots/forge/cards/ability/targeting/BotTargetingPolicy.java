package com.gempukku.lotro.bots.forge.cards.ability.targeting;

import com.gempukku.lotro.bots.forge.cards.abstractcards.BotCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.List;

public abstract class BotTargetingPolicy {
    public abstract List<BotCard> getTargets(List<List<BotCard>> possibleTargets, DefaultLotroGame game, String playerName);

    public abstract String toString();
}
