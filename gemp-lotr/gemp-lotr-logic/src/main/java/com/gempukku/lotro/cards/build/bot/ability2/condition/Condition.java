package com.gempukku.lotro.cards.build.bot.ability2.condition;

import com.gempukku.lotro.cards.build.bot.ability2.Target;
import com.gempukku.lotro.cards.build.bot.abstractcard.BotCard;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.game.state.PlannedBoardState;

import java.util.function.Predicate;

public abstract class Condition {
    public abstract boolean isOk(String player, PlannedBoardState plannedBoardState);

    public static ConditionSpotPlayableInDiscard spotPlayableInDiscard(Predicate<BotCard> target) {
        return new ConditionSpotPlayableInDiscard(target);
    }

    public static ConditionSpotPlayableInDiscard spotPlayableInDiscard(Culture culture, Race race) {
        return spotPlayableInDiscard(Target.and(Target.culture(culture), Target.race(race)));
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
