package com.gempukku.lotro.cards.build.bot.ability2.util;

import com.gempukku.lotro.cards.build.bot.abstractcard.BotCard;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.state.PlannedBoardState;

public class WoundsValueUtil {
    public static double evaluateWoundsChangeValue(String player, PlannedBoardState plannedBoardState, BotCard target, int amount) {
        if (target == null) return 0.0;

        boolean isCompanion = target.getSelf().getBlueprint().getCardType().equals(CardType.COMPANION);
        boolean isRingBearer = plannedBoardState.getRingBearers().contains(target);
        int vitality = plannedBoardState.getVitality(target);
        int wounds = plannedBoardState.getWounds(target);

        int realChange;
        if (amount > 0) {
            // wounding
            realChange = Math.min(amount, vitality );
        } else {
            // healing
            realChange = Math.max(amount, -wounds);
        }

        if (realChange > 0 && wounds == 0
                && plannedBoardState.getCurrentPhase().equals(Phase.FELLOWSHIP)
                && player.equals(plannedBoardState.getCurrentFpPlayer())
                && target.getSelf().getBlueprint().isUnique()) {
            int copiesInHand = (int) plannedBoardState.getHand(player).stream()
                    .filter(botCard -> botCard.getSelf().getBlueprint().getTitle().equals(target.getSelf().getBlueprint().getTitle()))
                    .count();
            if (copiesInHand > 0) {
                // if wounding a unique companion with no wounds during fellowship phase, modify exert value by number of copies in hand
                realChange -= Math.min(copiesInHand, realChange);
            }
        }

        if (realChange == 0) {
            return 0.0;
        }

        double value = Math.abs(realChange);

        if (!isCompanion) {
            value *= 0.5;
        }

        if (isCompanion) {
            int vitalityToEvaluate;
            if (realChange > 0) {
                // wounding: evaluate remaining vitality after wound
                vitalityToEvaluate = vitality - realChange;
            } else {
                // healing: evaluate initial vitality before healing
                vitalityToEvaluate = vitality;
            }

            // Assign value based on vitality
            if (vitalityToEvaluate == 0) {
                value += 1.0; // very valuable to deal the killing blow
            } else if (vitalityToEvaluate == 1) {
                value += 0.5; // last vitality point very valuable
            } else if (vitalityToEvaluate == 2) {
                value += 0.0; // 2 vitality standard value
            } else if (vitalityToEvaluate == 3) {
                value -= 0.3; // lower impact at 3 vitality
            } else if (vitalityToEvaluate >= 4) {
                value -= 0.5; // low impact at 4+ vitality
            }
        }

        if (isRingBearer) {
            value *= 0.35; // ring bearer wounds/heals are less impactful
        }

        boolean goodTargetingOwnCards = amount < 0;
        boolean targetsOwnCard = target.getSelf().getOwner().equals(player);
        if (goodTargetingOwnCards == targetsOwnCard) {
            return value;
        } else {
            return -value;
        }
    }
}
