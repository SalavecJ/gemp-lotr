package com.gempukku.lotro.cards.build.bot.ability;

import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.common.SitesBlock;
import com.gempukku.lotro.game.PhysicalCard;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class Condition {
    public static AbilityProperty wearingRing() {
        return new AbilityProperty(AbilityProperty.Type.WEARING_RING);
    }

    public static AbilityProperty phaseIs(Phase phase) {
        return new AbilityProperty(AbilityProperty.Type.PHASE_IS,
                new HashMap<>(Map.of("phase", phase)));
    }

    public static AbilityProperty twilightLessThan(int amount) {
        return new AbilityProperty(AbilityProperty.Type.TWILIGHT_LESS_THAN,
                new HashMap<>(Map.of("amount", amount)));
    }

    public static AbilityProperty burdensLessThan(int amount) {
        return new AbilityProperty(AbilityProperty.Type.BURDENS_LESS_THAN,
                new HashMap<>(Map.of("amount", amount)));
    }

    public static AbilityProperty skirmishingWith(Race race) {
        return new AbilityProperty(AbilityProperty.Type.SKIRMISHING_WITH,
                new HashMap<>(Map.of("race", race)));
    }

    public static AbilityProperty skirmishingWith(Keyword keyword) {
        return new AbilityProperty(AbilityProperty.Type.SKIRMISHING_WITH,
                new HashMap<>(Map.of("keyword", keyword)));
    }

    public static AbilityProperty hasKeyword(Keyword keyword) {
        return new AbilityProperty(AbilityProperty.Type.HAS_KEYWORD,
                new HashMap<>(Map.of("keyword", keyword)));
    }

    public static AbilityProperty spot(Predicate<PhysicalCard> spotRequirement) {
        return spot(spotRequirement, 1);
    }

    public static AbilityProperty spot(Predicate<PhysicalCard> spotRequirement, int numberToSpot) {
        return new AbilityProperty(AbilityProperty.Type.SPOT,
                new HashMap<>(Map.of("target", spotRequirement,
                        "numberToSpot", numberToSpot)));
    }

    public static AbilityProperty cannotSpot(Predicate<PhysicalCard> spotRequirement) {
        return new AbilityProperty(AbilityProperty.Type.CANNOT_SPOT,
                new HashMap<>(Map.of("target", spotRequirement)));
    }

    public static AbilityProperty isAt(Keyword keyword) {
        return new AbilityProperty(AbilityProperty.Type.CURRENT_SITE,
                new HashMap<>(Map.of("keyword", keyword)));
    }

    public static AbilityProperty isAt(int min, int max, SitesBlock sitesBlock) {
        return new AbilityProperty(AbilityProperty.Type.CURRENT_SITE,
                new HashMap<>(Map.of("min", min,
                        "max", max,
                        "block", sitesBlock)));
    }
}
