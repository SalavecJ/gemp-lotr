package com.gempukku.lotro.bots.forge.plan.action;

import com.gempukku.lotro.cards.build.bot.abstractcard.BotCard;
import com.gempukku.lotro.game.PhysicalCard;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;

public class UseCardWithTargetAction extends ChooseCardAction {
    private final List<Targeting> targetings;

    public UseCardWithTargetAction(PhysicalCard toUse, List<Targeting> targetings) {
        super(toUse);
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
        return "Action: Use " + getCard().getBlueprint().getFullName();
    }

    public record Targeting(BotCard target, Collection<BotCard> potentialTargets) {
    }
}
