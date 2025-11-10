package com.gempukku.lotro.bots.forge.cards.ability;

import com.gempukku.lotro.bots.forge.cards.BotTargetingMode;
import com.gempukku.lotro.bots.forge.cards.TriggerCondition;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

import java.util.List;

public class TriggeredAbility implements BotAbility {
    private final AbilityProperty effect;
    private final TriggerCondition triggerCondition;
    private final List<AbilityProperty> costs;
    private final List<AbilityProperty> conditions;
    private final boolean optional;

    public TriggeredAbility(AbilityProperty effect, TriggerCondition triggerCondition, List<AbilityProperty> costs, List<AbilityProperty> conditions, boolean optional) {
        this.effect = effect;
        this.triggerCondition = triggerCondition;
        this.costs = costs;
        this.conditions = conditions;
        this.optional = optional;
    }

    @Override
    public final AbilityType getType() {
        return AbilityType.TRIGGERED;
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
        if (costs.stream().anyMatch(abilityProperty -> abilityProperty.getType().equals(AbilityProperty.Type.EXERT))
                && awaitingDecision.getText().equals("Choose cards to exert")) {
            return BotTargetingMode.EXERT_SELF;
        }
        if (effect.getType().equals(AbilityProperty.Type.CHOICE)
                && awaitingDecision.getText().equals("Choose cards to exert")
                && (effect.getParam("optionA", AbilityProperty.class).getType().equals(AbilityProperty.Type.EXERT)
                || effect.getParam("optionB", AbilityProperty.class).getType().equals(AbilityProperty.Type.EXERT))) {
            return BotTargetingMode.EXERT_SELF;
        }
        if (effect.getType().equals(AbilityProperty.Type.MODIFY_STRENGTH)
                && awaitingDecision.getText().equals("Choose cards to modify strength of")) {
            return BotTargetingMode.SKIRMISHING_SIMPLE;
        }
        if (effect.getType().equals(AbilityProperty.Type.IF_ELSE)
                && awaitingDecision.getText().equals("Choose cards to modify strength of")
                && (effect.getParam("if", AbilityProperty.class).getType().equals(AbilityProperty.Type.MODIFY_STRENGTH)
                || effect.getParam("else", AbilityProperty.class).getType().equals(AbilityProperty.Type.MODIFY_STRENGTH))) {
            return BotTargetingMode.SKIRMISHING_SIMPLE;
        }
        if (effect.getType().equals(AbilityProperty.Type.MODIFY_STRENGTH)
                && costs.stream().anyMatch(abilityProperty -> abilityProperty.getType().equals(AbilityProperty.Type.EXERT_TARGET))
                && awaitingDecision.getText().equals("Choose cards to exert")) {
            return BotTargetingMode.SKIRMISHING_SIMPLE;
        }
        if (effect.getType().equals(AbilityProperty.Type.DISCARD_FROM_PLAY)
                && awaitingDecision.getText().equals("Choose cards to discard")) {
            return BotTargetingMode.HIGH_STRENGTH;
        }
        // TODO
        return null;
    }

    public TriggerCondition getTrigger() {
        return triggerCondition;
    }

    public boolean isOptional() {
        return optional;
    }

    public TriggerCondition getTriggerCondition() {
        return triggerCondition;
    }

    public List<AbilityProperty> getCosts() {
        return costs;
    }

    public List<AbilityProperty> getConditions() {
        return conditions;
    }
}
