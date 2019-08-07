package com.gempukku.lotro.cards.set40.wraith;

import com.gempukku.lotro.cards.AbstractResponseEvent;
import com.gempukku.lotro.cards.PlayConditions;
import com.gempukku.lotro.cards.TriggerConditions;
import com.gempukku.lotro.cards.actions.PlayEventAction;
import com.gempukku.lotro.cards.effects.choose.ChooseAndPlayCardFromDiscardEffect;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.timing.EffectResult;

import java.util.Collections;
import java.util.List;

/**
 * Title: Shadowy Pursuit
 * Set: Second Edition
 * Side: Shadow
 * Culture: Ringwraith
 * Twilight Cost: 0
 * Type: Event - Response
 * Card Number: 1U198
 * Game Text: If the Free Peoples player moves during the regroup phase, play a Nazgul from your discard pile.
 */
public class Card40_198 extends AbstractResponseEvent {
    public Card40_198() {
        super(Side.SHADOW, 0, Culture.WRAITH, "Shadowy Pursuit");
    }

    @Override
    public List<PlayEventAction> getOptionalAfterActions(String playerId, LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (TriggerConditions.moves(game, effectResult)
                && PlayConditions.isPhase(game, Phase.REGROUP)
                && PlayConditions.canPlayFromDiscard(playerId, game, Race.NAZGUL)
                && checkPlayRequirements(playerId, game, self, 0, 0, false, false)) {
            PlayEventAction action = new PlayEventAction(self);
            action.appendEffect(
                    new ChooseAndPlayCardFromDiscardEffect(playerId, game, Race.NAZGUL));
            return Collections.singletonList(action);
        }
        return null;
    }
}