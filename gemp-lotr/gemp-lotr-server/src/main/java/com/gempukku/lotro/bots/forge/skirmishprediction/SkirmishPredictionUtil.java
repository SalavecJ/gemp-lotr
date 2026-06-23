package com.gempukku.lotro.bots.forge.skirmishprediction;

public class SkirmishPredictionUtil {
    private static SkirmishResult.SkirmishResultEnum getSkirmishResult(int fpStrength, int shadowStrength) {
        return getSkirmishResult(fpStrength, shadowStrength, 2, 2);
    }

    private static SkirmishResult.SkirmishResultEnum getSkirmishResult(int fpStrength, int shadowStrength, int fpOverwhelmThreshold, int shadowOverwhelmThreshold) {
        if (fpStrength > shadowStrength) {
            if (fpStrength >= shadowOverwhelmThreshold * shadowStrength) {
                return SkirmishResult.SkirmishResultEnum.FP_OVERWHELM;
            }
            return SkirmishResult.SkirmishResultEnum.FP_WIN;
        } else {
            if (shadowStrength >= fpOverwhelmThreshold * fpStrength) {
                return SkirmishResult.SkirmishResultEnum.SHADOW_OVERWHELM;
            }
            return SkirmishResult.SkirmishResultEnum.SHADOW_WIN;
        }
    }

    public static SkirmishResult getSkirmishResult(int fpStrength, int shadowStrength, int fpDamageBonus, int shadowDamageBonus, int fpOverwhelmThreshold, int shadowOverwhelmThreshold) {
        int fpDamage = 1 + fpDamageBonus;
        int shadowDamage = 1 + shadowDamageBonus;
        SkirmishResult.SkirmishResultEnum result = getSkirmishResult(fpStrength, shadowStrength, fpOverwhelmThreshold, shadowOverwhelmThreshold);
        int damage = switch (result) {
            case FP_WIN -> fpDamage;
            case SHADOW_WIN -> shadowDamage;
            case FP_OVERWHELM, SHADOW_OVERWHELM, CANCELLED -> 0;
        };
        return new SkirmishResult(result, damage);
    }
}
