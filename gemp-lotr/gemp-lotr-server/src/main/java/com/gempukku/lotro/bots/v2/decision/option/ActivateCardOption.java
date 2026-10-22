package com.gempukku.lotro.bots.v2.decision.option;

import com.gempukku.lotro.game.PhysicalCard;

public class ActivateCardOption extends Option {
    private final PhysicalCard card;

    public ActivateCardOption(PhysicalCard card, int actionIndex) {
        super("Activate ability of " + card.getBlueprint().getFullName(), String.valueOf(actionIndex));
        this.card = card;
    }

    public PhysicalCard getCard() {
        return card;
    }
}
