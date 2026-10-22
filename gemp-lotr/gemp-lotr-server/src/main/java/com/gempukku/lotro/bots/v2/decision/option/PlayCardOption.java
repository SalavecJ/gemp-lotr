package com.gempukku.lotro.bots.v2.decision.option;

import com.gempukku.lotro.game.PhysicalCard;

public class PlayCardOption extends Option {
    private final PhysicalCard card;

    public PlayCardOption(PhysicalCard card, int actionIndex) {
        super("Play " + card.getBlueprint().getFullName(), String.valueOf(actionIndex));
        this.card = card;
    }

    public PhysicalCard getCard() {
        return card;
    }
}
