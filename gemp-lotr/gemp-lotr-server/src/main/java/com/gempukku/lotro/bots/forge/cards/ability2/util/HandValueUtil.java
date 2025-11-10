package com.gempukku.lotro.bots.forge.cards.ability2.util;

import com.gempukku.lotro.bots.forge.cards.ability2.EventAbility;
import com.gempukku.lotro.bots.forge.cards.abstractcard.BotCard;
import com.gempukku.lotro.bots.forge.cards.abstractcard.BotEventCard;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.bots.forge.plan.PlannedBoardState;

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

    public static double cardValueInHand(BotCard botCard, PlannedBoardState plannedBoardState) {
        if (botCard == null) {
            throw new IllegalArgumentException("BotCard cannot be null");
        }
        if (botCard instanceof BotEventCard eventCard) {
            EventAbility ability;
            try {
                ability = botCard.getEventAbility();
            } catch (IllegalStateException e) {
                // do not know what the card does, assume it has value
                return 1;
            }
            if (!eventCard.canBePlayedNoMatterThePhase(plannedBoardState)) {
                // event cannot be played, no value
                return 0;
            } else {
                return ability.getValueIfUsed(botCard.getSelf().getOwner(), plannedBoardState);
            }
        } else {
            if (!botCard.canBePlayedNoMatterThePhase(plannedBoardState)) {
                // dead card in hand
                return 0;
            } else {
                // do not know what the card does, assume
                return 1;
            }
        }
    }
}
