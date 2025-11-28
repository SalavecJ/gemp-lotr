package com.gempukku.lotro.bots.forge.cards.ability2.util;

import com.gempukku.lotro.bots.forge.cards.BotCardFactory;
import com.gempukku.lotro.bots.forge.cards.ability2.EventAbility;
import com.gempukku.lotro.bots.forge.cards.abstractcard.BotCard;
import com.gempukku.lotro.bots.forge.cards.abstractcard.BotEventCard;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

public class HandValueUtil {
    public static double cardValueInHand(PhysicalCard physicalCardCard, DefaultLotroGame game) {
        if (physicalCardCard == null) {
            throw new IllegalArgumentException("PhysicalCard cannot be null");
        }
        BotCard botCard = BotCardFactory.create(physicalCardCard);
        if (botCard instanceof BotEventCard eventCard) {
            EventAbility ability;
            try {
                ability = botCard.getEventAbility();
            } catch (IllegalStateException e) {
                // do not know what the card does, assume it has value
                return 1;
            }
            if (!eventCard.canBePlayedNoMatterThePhase(game)) {
                // event cannot be played, no value
                return 0;
            } else {
                return 1;
            }
        } else {
            if (!botCard.canBePlayedNoMatterThePhase(game)) {
                // dead card in hand
                return 0;
            } else {
                // do not know what the card does, assume
                return 1;
            }
        }
    }
}
