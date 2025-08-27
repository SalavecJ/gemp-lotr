package com.gempukku.lotro.cards.build.bot.ability;

import com.gempukku.lotro.game.PhysicalCard;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class AbilityProperty {
    private final Type type;
    private final Map<String, Object> params;

    public AbilityProperty(Type type) {
        this(type, new HashMap<>());
    }

    public AbilityProperty(Type type, Map<String, Object> params) {
        this.type = type;
        this.params = params;
    }

    public Type getType() {
        return type;
    }

    public <T> T getParam(String key, Class<T> clazz) {
        return clazz.cast(params.get(key));
    }

    public boolean hasParam(String key) {
        return params.containsKey(key);
    }

    public void addTargetPredicate(Predicate<PhysicalCard> value) {
        params.put("target", value);
    }

    public enum Type {
        // Helper for conditional effects
        IF_ELSE,
        CHOICE,

        // Effects
        WEAR_RING_UNTIL_REGROUP,
        PREVENT_WOUND_TO_BEARER_FOR_BURDEN,
        PREVENT_WOUND,
        REMOVE_BURDEN,
        MODIFY_STRENGTH,
        PUT_CARD_FROM_HAND_TO_BOTTOM_OF_DECK,
        PUT_CARD_FROM_DISCARD_INTO_HAND,
        PUT_CARD_FROM_DISCARD_TO_BOTTOM_OF_DECK,
        DISCARD_FROM_PLAY,
        REVEAL_OPPONENTS_HAND,
        LIMIT_WOUNDS_TAKEN_PER_PHASE,
        HEAL,
        OVERWHELM_MULTIPLIER,
        SHADOW_ARCHERY_MODIFIER,
        FP_ARCHERY_MODIFIER,
        SHADOW_NUMBER_MODIFIER,
        PLAY_NEXT_SITE,
        MODIFY_TWILIGHT_COST,
        MILL_CARDS,
        DRAW_CARDS,
        MAKE_SAM_RING_BEARER,
        MOVE_LIMIT_MODIFIER,
        CANCEL_SKIRMISH,
        PLAY_FROM_HAND,
        PLAY_FROM_DECK,
        PLAY_FROM_DISCARD,
        ADD_KEYWORD,

        // Costs
        ADD_TWILIGHT,
        DISCARD_SELF,
        EXERT_SELF,
        EXERT_TARGET,
        EXERT,
        REMOVE_TWILIGHT,
        DISCARD_FROM_HAND,

        // Conditions
        TWILIGHT_LESS_THAN,
        BURDENS_LESS_THAN,
        WEARING_RING,
        PHASE_IS,
        SKIRMISHING_WITH,
        SPOT,
        CANNOT_SPOT,
        CURRENT_SITE,
        HAS_KEYWORD
    }
}
