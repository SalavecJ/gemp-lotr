package com.gempukku.lotro.bots.forge.cards.ability2.condition;

import com.gempukku.lotro.bots.forge.cards.ability2.Target;
import com.gempukku.lotro.bots.forge.cards.abstractcard.BotCard;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.bots.forge.plan.PlannedBoardState;

import java.util.function.Predicate;

public abstract class Condition {
    public abstract boolean isOk(String player, PlannedBoardState plannedBoardState);

    public static ConditionSpotPlayableInDiscard spotPlayableInDiscard(Predicate<BotCard> target) {
        return new ConditionSpotPlayableInDiscard(target);
    }

    public static ConditionSpotPlayableInDiscard spotPlayableInDiscard(Culture culture, Race race) {
        return spotPlayableInDiscard(Target.and(Target.culture(culture), Target.race(race)));
    }

    public static ConditionSpotPossessionPlayableInDiscardOn spotPossessionPlayableInDiscardOn(Predicate<BotCard> possessionPredicate, Predicate<BotCard> bearerPredicate) {
        return new ConditionSpotPossessionPlayableInDiscardOn(possessionPredicate, bearerPredicate);
    }

    public static ConditionSpotAmount spotAmount(Predicate<BotCard> target, int amount) {
        return new ConditionSpotAmount(target, amount);
    }

    public static ConditionSpot spot(Predicate<BotCard> target) {
        return new ConditionSpot(target);
    }

    public static ConditionSpot spot(Keyword keyword) {
        return new ConditionSpot(Target.keyword(keyword));
    }

    public static ConditionSpot spot(String title) {
        return spot(Target.title(title));
    }

    public static ConditionSpot spot(Race race) {
        return spot(Target.race(race));
    }

    public static ConditionTwilight twilightLessThan(int amount) {
        return new ConditionTwilight(ConditionTwilight.TwilightState.LESS_THAN, amount);
    }

    public static ConditionTwilight twilightGreaterThan(int amount) {
        return new ConditionTwilight(ConditionTwilight.TwilightState.GREATER_THAN, amount);
    }
}
