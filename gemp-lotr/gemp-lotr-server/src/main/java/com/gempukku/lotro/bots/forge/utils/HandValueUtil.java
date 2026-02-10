package com.gempukku.lotro.bots.forge.utils;

import com.gempukku.lotro.bots.forge.cards.abstractcards.BotCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

public class HandValueUtil {

    public static double getHandValue(BotCard card, DefaultLotroGame game) {
        if (card.discardFromHandIfPossible(game)) {
            return -1.0;
        }
        //TODO
        return 1.0;
    }
}
