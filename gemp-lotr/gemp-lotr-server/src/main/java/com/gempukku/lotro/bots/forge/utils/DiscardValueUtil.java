package com.gempukku.lotro.bots.forge.utils;

import com.gempukku.lotro.bots.forge.cards.abstractcards.BotCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

public class DiscardValueUtil {

    public static double getDiscardValue(BotCard card, DefaultLotroGame game, String playerName) {
        int value = 1;

        if (card.getPhysicalCard().getOwner().equals(playerName)) {
            value = -value;
        }

        return value;
    }
}
