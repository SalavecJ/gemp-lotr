package com.gempukku.lotro.bots.forge.utils;

import com.gempukku.lotro.cards.build.bot.BotCardFactory;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

public class HandValueUtil {
    public static int scoreCardInHand(LotroGame game, PhysicalCard card, Side notWanted) {
        // TODO do something better

//        if (!BotCardFactory.create(card).canEverBePlayed()) {
//            return -100;
//        }

        int score = 0;

        if (notWanted.equals(card.getBlueprint().getSide())) {
            score -= 10;
        }

        score += switch (card.getBlueprint().getCardType()) {
            case THE_ONE_RING, SITE, MAP, ADVENTURE -> 0;
            case COMPANION -> 8;
            case ALLY -> 4;
            case MINION -> 6;
            case POSSESSION -> 5;
            case ARTIFACT -> 7;
            case EVENT -> 1;
            case CONDITION -> 3 + (card.getBlueprint().getSide().equals(Side.SHADOW) ? 4 : 0);
            case FOLLOWER -> 2;
        };

        score += switch (card.getBlueprint().getCardInfo().rarity) {
            case "R" -> 5;
            case "U" -> 2;
            default -> 0;
        };

        return score;
    }
}
