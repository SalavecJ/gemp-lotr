package com.gempukku.lotro.bots.forge.cards.ability;

import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.game.PhysicalCard;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class Cost {

    public static AbilityProperty addTwilight(int amount) {
        return new AbilityProperty(AbilityProperty.Type.ADD_TWILIGHT,
                new HashMap<>(Map.of("amount", amount)));
    }

    public static AbilityProperty removeTwilight(int amount) {
        return new AbilityProperty(AbilityProperty.Type.REMOVE_TWILIGHT,
                new HashMap<>(Map.of("amount", amount)));
    }

    public static AbilityProperty discardSelf() {
        return new AbilityProperty(AbilityProperty.Type.DISCARD_SELF);
    }

    public static AbilityProperty exertSelf() {
        return exertSelf(1);
    }

    public static AbilityProperty exertSelf(int amount) {
        return new AbilityProperty(AbilityProperty.Type.EXERT_SELF,
                new HashMap<>(Map.of("amount", amount)));
    }

    public static AbilityProperty exertTarget() {
        return exertTarget(1);
    }

    public static AbilityProperty exertTarget(int amount) {
        return new AbilityProperty(AbilityProperty.Type.EXERT_TARGET,
                new HashMap<>(Map.of("amount", amount)));
    }

    public static AbilityProperty exert(Predicate<PhysicalCard> target) {
        return exert(target, 1);
    }

    public static AbilityProperty exert(Predicate<PhysicalCard> target, int amount) {
        return new AbilityProperty(AbilityProperty.Type.EXERT,
                new HashMap<>(Map.of("target", target,
                        "amount", amount)));
    }

    public static AbilityProperty exert(String title) {
        return exert(title, 1);
    }

    public static AbilityProperty exert(String title, int amount) {
        return new AbilityProperty(AbilityProperty.Type.EXERT,
                new HashMap<>(Map.of("target", Target.title(title),
                        "amount", amount)));
    }

    public static AbilityProperty exertRanger() {
        return exertRanger(1);
    }

    public static AbilityProperty exertRanger(int amount) {
        return new AbilityProperty(AbilityProperty.Type.EXERT,
                new HashMap<>(Map.of("target", Target.keyword(Keyword.RANGER),
                        "amount", amount)));
    }


    public static AbilityProperty discardFromHand() {
        return discardFromHand(1);
    }

    public static AbilityProperty discardFromHand(int amount) {
        return new AbilityProperty(AbilityProperty.Type.DISCARD_FROM_HAND,
                new HashMap<>(Map.of("amount", amount)));
    }

    public static AbilityProperty discardFromHand(Predicate<PhysicalCard> target) {
        return discardFromHand(target, 1);
    }

    public static AbilityProperty discardFromHand(Predicate<PhysicalCard> target, int amount) {
        return new AbilityProperty(AbilityProperty.Type.DISCARD_FROM_HAND,
                new HashMap<>(Map.of("target", target,
                        "amount", amount)));
    }
}
