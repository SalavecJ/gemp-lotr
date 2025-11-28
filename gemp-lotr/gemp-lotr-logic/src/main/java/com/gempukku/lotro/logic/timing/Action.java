package com.gempukku.lotro.logic.timing;

import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

public interface Action {
    enum Type {
        PLAY_CARD, SPECIAL_ABILITY, TRIGGER, TRANSFER, RECONCILE, RESOLVE_DAMAGE, OTHER
    }

    Type getType();

    PhysicalCard getActionSource();

    void setActionTimeword(Timeword timeword);

    PhysicalCard getActionAttachedToCard();

    void setVirtualCardAction(boolean virtualCardAction);

    boolean isVirtualCardAction();

    void setPerformingPlayer(String playerId);

    String getPerformingPlayer();

    Timeword getActionTimeword();

    String getText(LotroGame game);

    Effect nextEffect(LotroGame game);
}
