package com.gempukku.lotro.cards.set15.elven;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.OptionalTriggerAction;
import com.gempukku.lotro.logic.cardtype.AbstractPermanent;
import com.gempukku.lotro.logic.effects.*;
import com.gempukku.lotro.logic.effects.choose.ChooseAndExertCharactersEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.PlayConditions;
import com.gempukku.lotro.logic.timing.TriggerConditions;

import java.util.*;

/**
 * Set: The Hunters
 * Side: Free
 * Culture: Elven
 * Twilight Cost: 2
 * Type: Artifact • Support Area
 * Game Text: To play, spot 3 Elves. Each time you are about to draw a card, you may exert an Elf to look at the top
 * three cards of your draw deck instead. Take a Free Peoples card into your hand and place the other cards on
 * the bottom of your draw deck.
 */
public class Card15_022 extends AbstractPermanent {
    public Card15_022() {
        super(Side.FREE_PEOPLE, 2, CardType.ARTIFACT, Culture.ELVEN, "The Mirror of Galadriel", "Dangerous Guide", true);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canSpot(game, 3, Race.ELF);
    }

    @Override
    public List<OptionalTriggerAction> getOptionalBeforeTriggers(final String playerId, final LotroGame game, Effect effect, final PhysicalCard self) {
        if (TriggerConditions.isDrawingACard(effect, game, playerId)
                && PlayConditions.canExert(self, game, Race.ELF)) {
            final OptionalTriggerAction action = new OptionalTriggerAction(self);
            action.appendCost(
                    new ChooseAndExertCharactersEffect(action, playerId, 1, 1, Race.ELF));
            action.appendEffect(
                    new PreventEffect((DrawOneCardEffect) effect));
            action.appendEffect(
                    new RevealTopCardsOfDrawDeckEffect(self, playerId, 3) {
                        @Override
                        protected void cardsRevealed(final List<PhysicalCard> revealedCards) {
                            action.appendEffect(
                                    new ChooseArbitraryCardsEffect(playerId, "Choose card to put into your hand", revealedCards, Side.FREE_PEOPLE, 1, 1) {
                                        @Override
                                        protected void cardsSelected(LotroGame game, Collection<PhysicalCard> selectedCards) {
                                            Set<PhysicalCard> cardsToPutOnBottom = new HashSet<>(revealedCards);
                                            for (PhysicalCard physicalCard : selectedCards) {
                                                cardsToPutOnBottom.remove(physicalCard);
                                                action.appendEffect(
                                                        new PutCardFromDeckIntoHandEffect(physicalCard));
                                            }

                                            action.appendEffect(
                                                    new PutCardsFromDeckBeneathDrawDeckEffect(action, self, playerId, cardsToPutOnBottom));
                                        }
                                    });
                        }
                    });
            return Collections.singletonList(action);
        }
        return null;
    }
}
