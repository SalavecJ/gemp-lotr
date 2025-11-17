package com.gempukku.lotro.bots.forge.plan.action;

import com.gempukku.lotro.bots.forge.cards.ability2.effect.EffectWithTarget;
import com.gempukku.lotro.bots.forge.cards.abstractcard.BotCard;
import com.gempukku.lotro.bots.forge.plan.PlannedBoardState;

import java.util.List;
import java.util.stream.Collectors;

public class ChooseTargetsForEffectAction extends ChooseTargetsAction {
    private final BotCard source;
    private final EffectWithTarget effect;

    public ChooseTargetsForEffectAction(List<BotCard> targets, BotCard source, EffectWithTarget effect) {
        super(targets);
        this.source = source;
        this.effect = effect;
    }

    public BotCard getSource() {
        return source;
    }

    public EffectWithTarget getEffect() {
        return effect;
    }

    public boolean targetsTheSameTypeOfTarget(ChooseTargetsForEffectAction other, PlannedBoardState plannedBoardState) {
        if (getTargets().size() != other.getTargets().size()) return false;

        // Create sorted lists of target characteristics for comparison
        List<String> thisTargetSignatures = getTargets().stream()
                .map(target -> target.getSelf().getBlueprint().getFullName() + "_" +
                        plannedBoardState.getStrength(target) + "_" +
                        plannedBoardState.getVitality(target))
                .sorted()
                .toList();

        List<String> otherTargetSignatures = other.getTargets().stream()
                .map(target -> target.getSelf().getBlueprint().getFullName() + "_" +
                        plannedBoardState.getStrength(target) + "_" +
                        plannedBoardState.getVitality(target))
                .sorted()
                .toList();

        return thisTargetSignatures.equals(otherTargetSignatures);
    }

    @Override
    public String toString() {
        String joined = getTargets().stream()
                .map(t -> t.getSelf().getBlueprint().getFullName())
                .sorted()
                .collect(Collectors.joining("; "));
        return "Action: Choose " + joined + " for effect of " + source.getSelf().getBlueprint().getFullName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChooseTargetsForEffectAction that = (ChooseTargetsForEffectAction) o;
        if (source.getSelf().getCardId() != that.source.getSelf().getCardId()) return false;
        if (getTargets().size() != that.getTargets().size()) return false;

        List<Integer> thisIds = getTargets().stream()
                .map(t -> t.getSelf().getCardId())
                .sorted()
                .toList();
        List<Integer> thatIds = that.getTargets().stream()
                .map(t -> t.getSelf().getCardId())
                .sorted()
                .toList();
        return thisIds.equals(thatIds);
    }

    @Override
    public int hashCode() {
        int result = Integer.hashCode(source.getSelf().getCardId());
        for (BotCard target : getTargets()) {
            result = 31 * result + Integer.hashCode(target.getSelf().getCardId());
        }
        return result;
    }
}
