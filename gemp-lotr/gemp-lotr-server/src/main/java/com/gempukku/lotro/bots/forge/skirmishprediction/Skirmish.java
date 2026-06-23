package com.gempukku.lotro.bots.forge.skirmishprediction;

import com.gempukku.lotro.bots.forge.cards.abstractcards.BotCard;
import com.gempukku.lotro.common.Keyword;

import java.util.List;
import java.util.Map;

public class Skirmish {
    private final BotCard fpCard;
    private final int fpVitality;
    private final Map<BotCard, Integer> minions; // Map of minion to its vitality

    public Skirmish(BotCard fpCard, int fpVitality, Map<BotCard, Integer> minions) {
        this.fpCard = fpCard;
        this.fpVitality = fpVitality;
        this.minions = minions;
    }

    public BotCard getFpCard() {
        return fpCard;
    }

    public List<BotCard> getMinions() {
        return minions.keySet().stream().toList();
    }

    public SkirmishResult predictSkirmishResult() {
        int fpStrength = fpCard.getPhysicalCard().getBlueprint().getStrength();
        int shadowStrength = minions.keySet().stream().mapToInt(value -> value.getPhysicalCard().getBlueprint().getStrength()).sum();
        int fpDamageBonus = fpCard.getPhysicalCard().getBlueprint().getKeywordCount(Keyword.DAMAGE);
        int shadowDamageBonus = minions.keySet().stream().mapToInt(value -> value.getPhysicalCard().getBlueprint().getKeywordCount(Keyword.DAMAGE)).sum();
        return SkirmishPredictionUtil.getSkirmishResult(fpStrength, shadowStrength, fpDamageBonus, shadowDamageBonus, 2, 2);
    }

    public boolean fpDies() {
        SkirmishResult result = predictSkirmishResult();
        if (result.getResult() == SkirmishResult.SkirmishResultEnum.SHADOW_OVERWHELM) {
            return true;
        } else if (result.getResult() == SkirmishResult.SkirmishResultEnum.SHADOW_WIN) {
            return result.getDamage() >= fpVitality;
        }
        return false;
    }

    public int fpVitalityLoss() {
        SkirmishResult result = predictSkirmishResult();
        if (result.getResult() == SkirmishResult.SkirmishResultEnum.SHADOW_OVERWHELM) {
            return fpVitality;
        } else if (result.getResult() == SkirmishResult.SkirmishResultEnum.SHADOW_WIN) {
            return Math.min(result.getDamage(), fpVitality);
        }
        return 0;
    }

    public int minionsKilled() {
        SkirmishResult result = predictSkirmishResult();
        int minionsKilled = 0;
        if (result.getResult() == SkirmishResult.SkirmishResultEnum.FP_OVERWHELM) {
            minionsKilled =  minions.size();
        } else if (result.getResult() == SkirmishResult.SkirmishResultEnum.FP_WIN) {
            int damage = result.getDamage();
            for (BotCard botCard : minions.keySet()) {
                if (minions.get(botCard) <= damage) {
                    minionsKilled++;
                }
            }
        }
        return minionsKilled;
    }

    public boolean allMinionsKilled() {
        return minionsKilled() == minions.size();
    }


}
