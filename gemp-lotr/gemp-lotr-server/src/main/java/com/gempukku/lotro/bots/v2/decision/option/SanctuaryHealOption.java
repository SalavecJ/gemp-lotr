package com.gempukku.lotro.bots.v2.decision.option;

import com.gempukku.lotro.game.PhysicalCard;

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
}
