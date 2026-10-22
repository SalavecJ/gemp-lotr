package com.gempukku.lotro.bots.v2.decision.option;

import com.gempukku.lotro.game.PhysicalCard;

public class ArcheryWoundOption extends Option {
    private final PhysicalCard target;


    public ArcheryWoundOption(PhysicalCard physicalCard) {
        super(getHumanReadableOption(physicalCard), String.valueOf(physicalCard.getCardId()));
        target = physicalCard;
    }

    private static String getHumanReadableOption(PhysicalCard physicalCard) {
        return "Wound " + physicalCard.getBlueprint().getFullName() + " with archery fire";
    }


    public PhysicalCard getTarget() {
        return target;
    }
}
