package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_02_044_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("business", "2_44");
					put("uruk", "1_151");

					put("mirror", "1_55");
					put("curse1", "1_36");
					put("curse2", "1_36");
					put("galadriel", "6_18");
					put("chaff1", "6_19");
					put("chaff2", "6_20");
					put("chaff3", "6_21");
					put("chaff4", "6_22");
					put("chaff5", "6_23");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NoBusinessofOursStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: No Business of Ours
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 0
		 * Type: Condition
		* Subtype: Support Area
		* Game Text: To play, spot an [isengard] minion. Plays to your support area.
		* 	The Free Peoples player may not look at or reveal cards in any Shadow player’s hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("business");

		assertEquals("No Business of Ours", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void NoBusinessSpotsAnIsengardMinionToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var business = scn.GetShadowCard("business");
		var uruk = scn.GetShadowCard("uruk");
		scn.MoveCardsToHand(business, uruk);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		assertFalse(scn.ShadowPlayAvailable(business));
		scn.ShadowPlayCard(uruk);
		assertTrue(scn.ShadowPlayAvailable(business));
	}

	@Test
	public void FreepsCannotRevealCardsInShadowHand() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var business = scn.GetShadowCard("business");
		scn.MoveCardsToHand(business);

		var galadriel = scn.GetFreepsCard("galadriel");
		var curse1 = scn.GetFreepsCard("curse1");
		var curse2 = scn.GetFreepsCard("curse2");
		scn.MoveCardsToHand(curse1, curse2);
		scn.MoveCardsToSupportArea(galadriel);

		scn.StartGame();
		scn.ShadowDrawCards(8);

		//First check that curse works with No Business in hand
		scn.FreepsPlayCard(curse1);
		assertTrue(scn.FreepsDecisionAvailable("Hand"));
		scn.MoveCardsToSupportArea(business); //slide in No Business between actions
		scn.FreepsDismissRevealedCards();

		//Curse's effect should now be blocked
		assertTrue(scn.FreepsPlayAvailable(curse2));
		scn.FreepsPlayCard(curse2);
		assertFalse(scn.FreepsDecisionAvailable("Hand"));
		assertTrue(scn.FreepsDecisionAvailable("Play Fellowship action"));
	}

	@Test
	public void FreepsCannotLookAtCardsInShadowHand() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var business = scn.GetShadowCard("business");
		scn.MoveCardsToHand(business);
		scn.MoveMinionsToTable("uruk");

		var galadriel = scn.GetFreepsCard("galadriel");
		var mirror = scn.GetFreepsCard("mirror");
		scn.MoveCardsToSupportArea(mirror, galadriel);

		scn.StartGame();
		scn.ShadowDrawCards(8);
		scn.SkipToPhase(Phase.MANEUVER);

		//First check that the mirror works with No Business in hand
		scn.FreepsUseCardAction(mirror);
		assertTrue(scn.FreepsDecisionAvailable("Opponent's hand"));
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseAny();

		scn.MoveCardsToSupportArea(business); //slide in No Business between actions
		scn.ShadowPassCurrentPhaseAction();

		//Mirror's effect should now be blocked
		assertTrue(scn.FreepsActionAvailable(mirror));
		scn.FreepsUseCardAction(mirror);
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.ShadowDecisionAvailable("Play Maneuver action"));
	}
}
