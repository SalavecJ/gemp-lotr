package com.gempukku.lotro.bots.forge.cards.ability;

import com.gempukku.lotro.bots.forge.cards.TriggerCondition;
import com.gempukku.lotro.game.PhysicalCard;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class AbilityBuilder {
    private final BotAbility.AbilityType type;

    private AbilityProperty effect;

    // Triggered
    private TriggerCondition triggerCondition;
    private boolean optional;

    private final List<AbilityProperty> costs = new ArrayList<>();
    private final List<AbilityProperty> conditions = new ArrayList<>();

    private AbilityBuilder(BotAbility.AbilityType type) {
        this.type = type;
    }

    public static AbilityBuilder activated() {
        return new AbilityBuilder(BotAbility.AbilityType.ACTIVATED);
    }

    public static AbilityBuilder triggered(TriggerCondition triggerCondition) {
        AbilityBuilder tbr = new AbilityBuilder(BotAbility.AbilityType.TRIGGERED);
        tbr.triggerCondition = triggerCondition;
        return tbr;
    }

    public static AbilityBuilder continuous() {
        return new AbilityBuilder(BotAbility.AbilityType.CONTINUOUS);
    }

    public static AbilityBuilder onPlay() {
        AbilityBuilder tbr = new AbilityBuilder(BotAbility.AbilityType.TRIGGERED);
        tbr.triggerCondition = TriggerCondition.WHEN_PLAYED;
        tbr.optional(false);
        return tbr;
    }

    public AbilityBuilder effect(AbilityProperty effect) {
        this.effect = effect;
        return this;
    }

    public AbilityBuilder cost(AbilityProperty cost) {
        costs.add(cost);
        return this;
    }

    public AbilityBuilder condition(AbilityProperty condition) {
        conditions.add(condition);
        return this;
    }

    public AbilityBuilder optional(boolean optional) {
        this.optional = optional;
        return this;
    }

    public AbilityBuilder target(Predicate<PhysicalCard> targetPredicate) {
        effect.addTargetPredicate(targetPredicate);
        return this;
    }

    public AbilityBuilder targetForOptionA(Predicate<PhysicalCard> targetPredicate) {
        effect.getParam("optionA", AbilityProperty.class).addTargetPredicate(targetPredicate);
        return this;
    }

    public AbilityBuilder targetForOptionB(Predicate<PhysicalCard> targetPredicate) {
        effect.getParam("optionB", AbilityProperty.class).addTargetPredicate(targetPredicate);
        return this;
    }

    public BotAbility build() {
        return switch (type) {
            case TRIGGERED -> new TriggeredAbility(effect, triggerCondition, costs, conditions, optional);
            case ACTIVATED -> new ActivatedAbility(effect, costs, conditions);
            case CONTINUOUS -> new ContinuousAbility(effect, conditions);
        };
    }
}
