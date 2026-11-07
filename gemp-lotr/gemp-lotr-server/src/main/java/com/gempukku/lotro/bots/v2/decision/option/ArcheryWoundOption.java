package com.gempukku.lotro.bots.v2.decision.option;

import com.gempukku.lotro.game.PhysicalCard;

import java.util.Objects;

public class ArcheryWoundOption extends Option {
    private final PhysicalCard target;
    private final int targetId;


    public ArcheryWoundOption(PhysicalCard physicalCard) {
        super(getHumanReadableOption(physicalCard), String.valueOf(physicalCard.getCardId()));
        target = physicalCard;
        this.targetId = target.getCardId();
    }

    private static String getHumanReadableOption(PhysicalCard physicalCard) {
        return "Wound " + physicalCard.getBlueprint().getFullName() + " with archery fire";
    }

    public PhysicalCard getTarget() {
        return target;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ArcheryWoundOption that = (ArcheryWoundOption) o;
        return Objects.equals(targetId, that.targetId)
                && Objects.equals(target.getBlueprint().getFullName(), that.target.getBlueprint().getFullName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetId, target.getBlueprint().getFullName());
    }
}
