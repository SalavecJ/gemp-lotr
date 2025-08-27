package com.gempukku.lotro.cards.build.bot;

public enum BotTargetingMode {
    SPECIAL("Special targeting rules needed"),
    RANDOM("Random"),
    SKIRMISHING_SIMPLE("Skirmishing Simple"),
    COMPANION_HIGH_STRENGTH("Companion with the highest strength"),
    COMPANION_LOW_STRENGTH("Companion with the lowest strength"),
    COMPANION_NOT_DYING("Companion least likely to die"),
    HIGH_STRENGTH("Character with the highest strength"),
    LOW_STRENGTH("Character with the lowest strength"),
    HEAL("Heal"),
    EXERT_SELF("Self-exerting targeting"),
    LOW_VALUE_CARD_IN_HAND_PREF_FP("Lowest value card in hand, preferably FP"),
    LOW_VALUE_CARD_IN_HAND_PREF_SHADOW("Lowest value card in hand, preferably shadow");

    private final String humanReadable;

    BotTargetingMode(String humanReadable) {
        this.humanReadable = humanReadable;
    }

    @Override
    public String toString() {
        return humanReadable;
    }
}
