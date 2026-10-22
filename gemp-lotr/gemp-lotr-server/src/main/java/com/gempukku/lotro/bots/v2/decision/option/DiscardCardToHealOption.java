package com.gempukku.lotro.bots.v2.decision.option;

import com.gempukku.lotro.game.PhysicalCard;

public class DiscardCardToHealOption extends Option {
    private final PhysicalCard card;

    public DiscardCardToHealOption(PhysicalCard card, int actionIndex) {
        super("Discard " + card.getBlueprint().getFullName() + " to heal character in play", String.valueOf(actionIndex));
        this.card = card;
    }

    public PhysicalCard getCard() {
        return card;
    }
}
