package com.gempukku.lotro.bots.v2.decision.option;

import com.gempukku.lotro.game.PhysicalCard;

public class DiscardToReconcileOption extends Option {
    private final PhysicalCard physicalCard;

    public DiscardToReconcileOption(PhysicalCard physicalCard) {
        super("Discard " + physicalCard.getBlueprint().getFullName(), String.valueOf(physicalCard.getCardId()));
        this.physicalCard = physicalCard;
    }

    public PhysicalCard getPhysicalCard() {
        return physicalCard;
    }
}
