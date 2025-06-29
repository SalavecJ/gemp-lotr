
package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V1_037_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>() {{
					put("fell", "101_37");
					put("fell2", "101_37");
					put("nazgul", "1_232");
					put("blade", "1_216");
					put("ring", "9_44");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FellVoicesCallStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: V1
		* Title: Fell Voices Call
		* Side: Free Peoples
		* Culture: ringwraith
		* Twilight Cost: 0
		* Type: event
		* Subtype: Shadow
		* Game Text: Play a [ringwraith] item from your draw deck or discard pile.
		*/

		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl fell = scn.GetFreepsCard("fell");

		assertFalse(fell.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, fell.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, fell.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, fell.getBlueprint().getCardType());
		//assertEquals(Race.CREATURE, fell.getBlueprint().getRace());
        assertTrue(scn.HasTimeword(fell, Timeword.SHADOW)); // test for keywords as needed
		assertEquals(0, fell.getBlueprint().getTwilightCost());
		//assertEquals(, fell.getBlueprint().getStrength());
		//assertEquals(, fell.getBlueprint().getVitality());
		//assertEquals(, fell.getBlueprint().getResistance());
		//assertEquals(Signet., fell.getBlueprint().getSignet());
		//assertEquals(, fell.getBlueprint().getSiteNumber()); // Change this to getAllyHomeSiteNumbers for allies

	}

	@Test
	public void FellVoicesCallPullsItemsFromDiscardOrDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl fell = scn.GetShadowCard("fell");
		PhysicalCardImpl fell2 = scn.GetShadowCard("fell2");
		PhysicalCardImpl nazgul = scn.GetShadowCard("nazgul");
		PhysicalCardImpl blade = scn.GetShadowCard("blade");
		PhysicalCardImpl ring = scn.GetShadowCard("ring");
		scn.MoveCardsToHand(fell, fell2);
		scn.MoveCompanionToTable(nazgul);
		scn.MoveCardsToDiscard(ring);
		scn.MoveCardsToTopOfDeck(blade);

		scn.StartGame();

		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.ShadowPlayAvailable(fell));
		assertEquals(Zone.DISCARD, ring.getZone());
		assertEquals(Zone.DECK, blade.getZone());
		scn.ShadowPlayCard(fell);

		assertTrue(scn.ShadowDecisionAvailable("Choose action to perform"));
		assertEquals(2, scn.ShadowGetMultipleChoices().size());
		scn.ShadowChooseOption("discard");
		assertEquals(Zone.ATTACHED, ring.getZone());
		assertEquals(nazgul, ring.getAttachedTo());

		assertTrue(scn.ShadowPlayAvailable(fell2));
		scn.ShadowPlayCard(fell2);
		scn.ShadowDismissRevealedCards();

        scn.ShadowChooseCardBPFromSelection(blade);
		assertEquals(Zone.ATTACHED, blade.getZone());
		assertEquals(nazgul, blade.getAttachedTo());

	}
}
