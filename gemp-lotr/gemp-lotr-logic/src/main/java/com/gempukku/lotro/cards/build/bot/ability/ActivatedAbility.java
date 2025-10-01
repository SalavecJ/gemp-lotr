package com.gempukku.lotro.cards.build.bot.ability;

import com.gempukku.lotro.cards.build.bot.BotTargetingMode;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

import java.util.List;

public class ActivatedAbility implements BotAbility {
    private final AbilityProperty effect;
    private final List<AbilityProperty> costs;
    private final List<AbilityProperty> conditions;

    public ActivatedAbility(AbilityProperty effect, List<AbilityProperty> costs, List<AbilityProperty> conditions) {
        this.effect = effect;
        this.costs = costs;
        this.conditions = conditions;
    }

    @Override
    public final AbilityType getType() {
        return AbilityType.ACTIVATED;
    }

    @Override
    public AbilityProperty getEffect() {
        return effect;
    }

    @Override
    public boolean canProduceDecision(LotroGame game, AwaitingDecision awaitingDecision) {
        return getTargetingModeForDecision(game, awaitingDecision) != null;
    }

    @Override
    public BotTargetingMode getTargetingModeForDecision(LotroGame game, AwaitingDecision awaitingDecision) {
        if (effect.getType().equals(AbilityProperty.Type.PLAY_FROM_HAND)
                && awaitingDecision.getText().equals("Choose card to play from hand")) {
            return BotTargetingMode.COMPANION_HIGH_STRENGTH;
        }
        if (effect.getType().equals(AbilityProperty.Type.OVERWHELM_MULTIPLIER)
                && awaitingDecision.getText().equals("Choose characters to apply overwhelm multiplier on")) {
            return BotTargetingMode.SKIRMISHING_SIMPLE;
        }
        if (effect.getType().equals(AbilityProperty.Type.MODIFY_STRENGTH)
                && costs.stream().anyMatch(abilityProperty -> abilityProperty.getType().equals(AbilityProperty.Type.EXERT_TARGET))
                && awaitingDecision.getText().equals("Choose cards to exert")) {
            return BotTargetingMode.SKIRMISHING_SIMPLE;
        }
        if (effect.getType().equals(AbilityProperty.Type.PUT_CARD_FROM_HAND_TO_BOTTOM_OF_DECK)
                && awaitingDecision.getText().equals("Choose cards from hand")) {
            return BotTargetingMode.LOW_VALUE_CARD_IN_HAND_PREF_SHADOW;
        }
        if (effect.getType().equals(AbilityProperty.Type.MODIFY_STRENGTH)
                && awaitingDecision.getText().equals("Choose cards to modify strength of")) {
            return BotTargetingMode.SKIRMISHING_SIMPLE;
        }
        if (effect.getType().equals(AbilityProperty.Type.MODIFY_STRENGTH)
                && awaitingDecision.getText().equals("Choose cards to modify strength of")) {
            return BotTargetingMode.SKIRMISHING_SIMPLE;
        }
        if (costs.stream().anyMatch(abilityProperty -> abilityProperty.getType().equals(AbilityProperty.Type.EXERT))
                && awaitingDecision.getText().equals("Choose cards to exert")) {
            return BotTargetingMode.EXERT_SELF;
        }
        if (costs.stream().anyMatch(abilityProperty -> abilityProperty.getType().equals(AbilityProperty.Type.DISCARD_FROM_HAND))
                && awaitingDecision.getText().equals("Choose cards from hand to discard")) {
            return BotTargetingMode.LOW_VALUE_CARD_IN_HAND_PREF_FP;
        }
        if (effect.getType().equals(AbilityProperty.Type.HEAL)
                && awaitingDecision.getText().equals("Choose cards to heal")) {
            return BotTargetingMode.HEAL;
        }
        if (effect.getType().equals(AbilityProperty.Type.ADD_KEYWORD)
                && effect.hasParam("untilRegroup")
                && effect.getParam("untilRegroup", Boolean.class)
                && costs.stream().anyMatch(abilityProperty -> abilityProperty.getType().equals(AbilityProperty.Type.EXERT_TARGET))
                && awaitingDecision.getText().equals("Choose cards to exert")) {
            return BotTargetingMode.SPECIAL;
        }
        // TODO
        return null;
    }

    public List<AbilityProperty> getCosts() {
        return costs;
    }

    public List<AbilityProperty> getConditions() {
        return conditions;
    }
}
