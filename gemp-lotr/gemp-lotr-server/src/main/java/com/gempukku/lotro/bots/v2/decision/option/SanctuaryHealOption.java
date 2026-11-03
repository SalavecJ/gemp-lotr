package com.gempukku.lotro.bots.v2.decision.option;

import com.gempukku.lotro.game.PhysicalCard;

import java.util.Objects;

public class SanctuaryHealOption extends Option {
    private final PhysicalCard target;


    public SanctuaryHealOption(PhysicalCard physicalCard) {
        super(getHumanReadableOption(physicalCard), String.valueOf(physicalCard.getCardId()));
        target = physicalCard;
    }

    private static String getHumanReadableOption(PhysicalCard physicalCard) {
        return "Heal " + physicalCard.getBlueprint().getFullName() + " on sanctuary site";
    }


    public PhysicalCard getTarget() {
        return target;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SanctuaryHealOption that = (SanctuaryHealOption) o;
        return Objects.equals(target.getCardId(), that.target.getCardId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(target.getCardId());
    }
}
