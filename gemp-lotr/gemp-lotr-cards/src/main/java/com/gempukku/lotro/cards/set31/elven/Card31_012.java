package com.gempukku.lotro.cards.set31.elven;

import com.gempukku.lotro.cards.AbstractAlly;
import com.gempukku.lotro.cards.PlayConditions;
import com.gempukku.lotro.cards.effects.AddUntilStartOfPhaseModifierEffect;
import com.gempukku.lotro.cards.effects.ChoiceEffect;
import com.gempukku.lotro.cards.effects.DiscardBottomCardFromDeckEffect;
import com.gempukku.lotro.cards.effects.choose.ChooseAndDiscardCardsFromPlayEffect;
import com.gempukku.lotro.cards.effects.choose.ChooseAndExertCharactersEffect;
import com.gempukku.lotro.cards.modifiers.AllyParticipatesInArcheryFireAndSkirmishesModifier;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.effects.ChooseActiveCardEffect;
import com.gempukku.lotro.logic.modifiers.ModifiersQuerying;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.Action;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Set: The Short Rest
 * Side: Free
 * Culture: Elven
 * Twilight Cost: 4
 * Type: Ally • Home 5 • Elf
 * Strength: 9
 * Vitality: 4
 * Site: 5
 * Game Text: Maneuver: Exert Thorin twice or discard a [DWARVEN] artifact to allow Thranduil
 * to participate in archery fire and skirmishes until the regroup phase.
 */
public class Card31_012 extends AbstractAlly {
    public Card31_012() {
        super(4, Block.HOBBIT, 5, 9, 4, Race.ELF, Culture.ELVEN, "Thranduil", "Elven King", true);
    }

    @Override
    protected List<? extends Action> getExtraInPlayPhaseActions(String playerId, LotroGame game, PhysicalCard self) {
        if (PlayConditions.canUseFPCardDuringPhase(game, Phase.MANEUVER, self)
                && (PlayConditions.canExert(self, game, 2, Filters.name("Thorin"))
                || PlayConditions.canDiscardFromPlay(self, game, Culture.DWARVEN, CardType.ARTIFACT))) {
            ActivateCardAction action = new ActivateCardAction(self);
            List<Effect> possibleCosts = new LinkedList<Effect>();
            possibleCosts.add(
                    new ChooseAndExertCharactersEffect(action, playerId, 1, 1, 2, Filters.name("Thorin")) {
                        @Override
                        public String getText(LotroGame game) {
                            return "Exert Thorin twice";
                        }
                    });
            possibleCosts.add(
                    new ChooseAndDiscardCardsFromPlayEffect(action, playerId, 1, 1, Culture.DWARVEN, CardType.ARTIFACT) {
                        @Override
                        public String getText(LotroGame game) {
                            return "Discard a DWARVEN artifact";
                        }
                    });
            action.appendCost(
                    new ChoiceEffect(action, playerId, possibleCosts));
            action.appendEffect(
                    new AddUntilStartOfPhaseModifierEffect(
                            new AllyParticipatesInArcheryFireAndSkirmishesModifier(self, self), Phase.REGROUP));
            return Collections.singletonList(action);
        }
        return null;
    }
}