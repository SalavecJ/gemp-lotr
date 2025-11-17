package com.gempukku.lotro.bots.forge.cards.ability2;

import com.gempukku.lotro.bots.forge.cards.ability2.condition.Condition;
import com.gempukku.lotro.bots.forge.cards.ability2.cost.Cost;
import com.gempukku.lotro.bots.forge.cards.ability2.cost.CostWithTarget;
import com.gempukku.lotro.bots.forge.cards.ability2.effect.Effect;
import com.gempukku.lotro.bots.forge.cards.ability2.effect.EffectWithTarget;
import com.gempukku.lotro.bots.forge.cards.abstractcard.BotCard;
import com.gempukku.lotro.bots.forge.plan.PlannedBoardState;

import java.util.List;

public abstract class Ability {
    protected final Effect effect;
    protected final Cost cost;
    protected final List<Condition> conditions;

    public Ability(Effect effect, Cost cost, List<Condition> conditions) {
        this.effect = effect;
        this.cost = cost;
        this.conditions = conditions;
    }

    public final Effect getEffect() {
        return effect;
    }

    public final Cost getCost() {
        return cost;
    }

    public final void resolveAbility(String player, PlannedBoardState plannedBoardState) {
        if (cost != null) {
            if (cost instanceof CostWithTarget) {
                throw new IllegalStateException("Cost requires target to pay.");
            }
            cost.pay(player, plannedBoardState);
        }
        if (effect instanceof EffectWithTarget) {
            throw new IllegalStateException("Effect requires target to resolve.");
        }
        effect.resolve(player, plannedBoardState);
    }

    public final void resolveAbilityWithCostTargets(String player, PlannedBoardState plannedBoardState, List<BotCard> costTargets) {
        if (cost != null && cost instanceof CostWithTarget costWithTarget) {
            costWithTarget.payWithTargets(player, plannedBoardState, costTargets);
            if (effect instanceof EffectWithTarget) {
                throw new IllegalStateException("Effect requires target to resolve.");
            }
            effect.resolve(player, plannedBoardState);
        } else {
            if (cost == null)
                throw new IllegalStateException("No cost to target for paying with target.");
            throw new IllegalStateException("Cost targeting for this cost not supported: " + cost.getClass().getSimpleName());
        }
    }

    public final void resolveAbilityOnTargets(String player, PlannedBoardState plannedBoardState, List<BotCard> targets) {
        if (effect instanceof EffectWithTarget effectWithTarget) {
            if (cost != null) {
                if (cost instanceof CostWithTarget) {
                    throw new IllegalStateException("Cost requires target to pay.");
                }
                cost.pay(player, plannedBoardState);
            }
            effectWithTarget.resolveWithTargets(player, plannedBoardState, targets);
        } else {
            throw new IllegalStateException("Targeting for this effect not supported: " + effect.getClass().getSimpleName());
        }
    }

    public final void resolveAbilityOnTargetsWithCostTargets(String player, PlannedBoardState plannedBoardState, List<BotCard> effectTargets, List<BotCard> costTargets) {
        if (effect instanceof EffectWithTarget effectWithTarget) {
            if (cost != null && cost instanceof CostWithTarget costWithTarget) {
                costWithTarget.payWithTargets(player, plannedBoardState, costTargets);
                effectWithTarget.resolveWithTargets(player, plannedBoardState, effectTargets);
            } else {
                if (cost == null)
                    throw new IllegalStateException("No cost to target for paying with target.");
                throw new IllegalStateException("Cost targeting for this cost not supported: " + cost.getClass().getSimpleName());
            }
        } else {
            throw new IllegalStateException("Targeting for this effect not supported: " + effect.getClass().getSimpleName());
        }
    }

    public final boolean canPayCost(String player, PlannedBoardState plannedBoardState) {
        if (cost == null) return true;
        return cost.canPayCost(player, plannedBoardState);
    }

    public final double getPossibleValue(String player, PlannedBoardState plannedBoardState) {
        double tbr = 0.0;
        if (cost != null) {
            if (cost instanceof CostWithTarget costWithTarget) {
                tbr += costWithTarget.getMaximumPossibleValue(player, plannedBoardState);
            } else {
                tbr += cost.getValueIfPayed(player, plannedBoardState);
            }
        }
        if (effect instanceof EffectWithTarget effectWithTarget) {
            tbr += effectWithTarget.getMaximumPossibleValue(player, plannedBoardState);
        } else {
            tbr += effect.getValueIfResolved(player, plannedBoardState);
        }
        return tbr;
    }

    public final boolean conditionsOk(String player, PlannedBoardState plannedBoardState) {
        if (conditions == null || conditions.isEmpty()) return true;
        return conditions.stream().allMatch(condition -> condition.isOk(player, plannedBoardState));
    }

}
