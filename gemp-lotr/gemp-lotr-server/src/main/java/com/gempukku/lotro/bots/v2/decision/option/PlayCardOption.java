package com.gempukku.lotro.bots.v2.decision.option;

import com.gempukku.lotro.game.PhysicalCard;

import java.util.Objects;

public class PlayCardOption extends Option {
    private final PhysicalCard card;
    private final int cardId;

    public PlayCardOption(PhysicalCard card, int actionIndex) {
        super("Play " + card.getBlueprint().getFullName(), String.valueOf(actionIndex));
        this.card = card;
        this.cardId = card.getCardId();
    }

    public PhysicalCard getCard() {
        return card;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PlayCardOption that = (PlayCardOption) o;
        return Objects.equals(cardId, that.cardId) && Objects.equals(card.getBlueprint().getFullName(), that.card.getBlueprint().getFullName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(cardId, card.getBlueprint().getFullName());
    }
}
