package com.gempukku.lotro.bots.v2.decision.option;

import com.gempukku.lotro.game.PhysicalCard;

public class TransferCardOption extends Option {
    private final PhysicalCard card;

    public TransferCardOption(PhysicalCard card, int actionIndex) {
        super("Transfer " + card.getBlueprint().getFullName(), String.valueOf(actionIndex));
        this.card = card;
    }

    public PhysicalCard getCard() {
        return card;
    }
}
