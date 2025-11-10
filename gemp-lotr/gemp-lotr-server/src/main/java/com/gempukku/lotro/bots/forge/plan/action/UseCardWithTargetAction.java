package com.gempukku.lotro.bots.forge.plan.action;

import com.gempukku.lotro.bots.forge.cards.ability2.effect.Effect;
import com.gempukku.lotro.bots.forge.cards.abstractcard.BotCard;
import com.gempukku.lotro.game.PhysicalCard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class UseCardWithTargetAction extends UseCardAction {
    private final List<Targeting> targetings;

    public UseCardWithTargetAction(BotCard toUse, List<Targeting> targetings, Class<? extends Effect> effectClass) {
        super(toUse, effectClass);
        this.targetings = targetings;
    }

    public PhysicalCard getTarget(List<Integer> physicalIds) {
        for (Targeting targeting : targetings) {
            HashSet<Integer> targetingIds = new HashSet<>();
            for (BotCard potentialTarget : targeting.potentialTargets) {
                targetingIds.add(potentialTarget.getSelf().getCardId());
            }
            if (targetingIds.containsAll(physicalIds) && physicalIds.containsAll(targetingIds)) {
                return targeting.target.getSelf();
            }
        }
        if (targetings.size() == 1) {
            throw new IllegalStateException("Could not find target for this decision. Expected ids: " + targetings.getFirst().potentialTargets().stream().map(botCard -> botCard.getSelf().getCardId()).toList());
        }

        throw new IllegalStateException("Could not find target for this decision");
    }

    @Override
    public String toString() {
        List<String> targetStrings = new ArrayList<>();
        for (Targeting targeting : targetings) {
            String joined = targeting.potentialTargets.stream()
                    .map(t -> t.getSelf().getBlueprint().getFullName())
                    .collect(Collectors.joining("; "));
            targetStrings.add("Choose " + targeting.target.getSelf().getBlueprint().getFullName() + " from [" + joined + "]");

        }
        StringBuilder builder = new StringBuilder("Action: Use " + getPhysicalCard().getBlueprint().getFullName());
        for (String targetString : targetStrings) {
            builder.append("\n").append(targetString);
        }
        return builder.toString();
    }

    public record Targeting(BotCard target, Collection<BotCard> potentialTargets) {
    }
}
