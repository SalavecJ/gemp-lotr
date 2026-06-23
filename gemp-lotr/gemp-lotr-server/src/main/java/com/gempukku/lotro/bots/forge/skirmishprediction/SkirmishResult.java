package com.gempukku.lotro.bots.forge.skirmishprediction;

public class SkirmishResult {

    private final SkirmishResultEnum result;
    private final int damage;

    public SkirmishResult(SkirmishResultEnum result, int damage) {
        this.result = result;
        this.damage = damage;
    }

    public SkirmishResultEnum getResult() {
        return result;
    }

    public int getDamage() {
        return damage;
    }


    public enum SkirmishResultEnum {
        FP_WIN, SHADOW_WIN, FP_OVERWHELM, SHADOW_OVERWHELM, CANCELLED
    }
}
