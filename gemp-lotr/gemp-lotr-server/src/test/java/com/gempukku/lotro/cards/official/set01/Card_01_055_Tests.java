package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_055_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mirror", "1_55");
					put("galadriel", "1_45");
					put("allyHome3_1", "1_60");
					put("allyHome6_1", "1_56");

					put("runner", "1_178");
					put("card1", "1_179");
					put("card2", "1_180");
					put("card3", "1_181");
					put("card4", "1_182");
					put("card5", "1_183");
					put("card6", "1_184");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheMirrorofGaladrielStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: The Mirror of Galadriel
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.
		 * 	Each Elf ally whose home is site 6 is strength +1.
		 * 	Maneuver: If an opponent has at least 7 cards in hand, exert Galadriel to look at
		 * 	2 of those cards at random. Discard one and replace the other.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("mirror");

		assertEquals("The Mirror of Galadriel", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MirrorMakesSite6AlliesStrengthPlus1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var mirror = scn.GetFreepsCard("mirror");
		var galadriel = scn.GetFreepsCard("galadriel");
		var allyHome3_1 = scn.GetFreepsCard("allyHome3_1");
		var allyHome6_1 = scn.GetFreepsCard("allyHome6_1");
		scn.MoveCompanionToTable(galadriel, allyHome3_1, allyHome6_1);
		scn.MoveCardsToHand(mirror);

		scn.StartGame();

		assertEquals(5, scn.GetStrength(allyHome3_1));
		assertEquals(3, scn.GetStrength(galadriel));
		assertEquals(3, scn.GetStrength(allyHome6_1));
		scn.FreepsPlayCard(mirror);
		assertEquals(5, scn.GetStrength(allyHome3_1));
		assertEquals(4, scn.GetStrength(galadriel));
		assertEquals(4, scn.GetStrength(allyHome6_1));
	}

	@Test
	public void ManeuverAbilityNotAvailableIfShadowHandLessThan7() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var mirror = scn.GetFreepsCard("mirror");
		var galadriel = scn.GetFreepsCard("galadriel");
		scn.MoveCompanionToTable(galadriel);
		scn.MoveCardsToSupportArea(mirror);

		scn.MoveCardsToShadowHand("card1", "card2", "card3", "card4", "card5", "card6");
		scn.MoveMinionsToTable("runner");

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);
		assertFalse(scn.FreepsActionAvailable(mirror));
	}

	@Test
	public void ManeuverAbilityReveals2CardFromShadowHandAndDiscards1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var mirror = scn.GetFreepsCard("mirror");
		var galadriel = scn.GetFreepsCard("galadriel");
		scn.MoveCompanionToTable(galadriel);
		scn.MoveCardsToSupportArea(mirror);

		scn.MoveCardsToShadowHand("card1", "card2", "card3", "card4", "card5", "card6", "mirror");
		scn.MoveMinionsToTable("runner");

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsActionAvailable(mirror));
		assertEquals(0, scn.GetWoundsOn(galadriel));
		assertEquals(7, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		//assertEquals(Zone.HAND, card1.getZone());

		scn.FreepsUseCardAction(mirror);
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		scn.FreepsDismissRevealedCards();
		scn.ShadowDismissRevealedCards();

		scn.FreepsChooseAny();
		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertEquals(6, scn.GetShadowHandCount());
		assertEquals(1, scn.GetShadowDiscardCount());
		//assertEquals(Zone.DISCARD, card1.getZone());
	}

}
