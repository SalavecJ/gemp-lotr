
package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Card_V1_038_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>() {{
					put("harry", "101_38");
					put("rider", "12_161");
					put("wk", "1_237");

					put("farin", "1_11");
					put("aragorn", "1_89");
					put("pippin", "1_307");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HarryGoatleafStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: V1
		* Title: *Harry Goatleaf, In Thrall to the Nine
		* Side: Free Peoples
		* Culture: ringwraith
		* Twilight Cost: 1
		* Type: minion
		* Subtype: Man
		* Strength: 3
		* Vitality: 1
		* Site Number: 2
		* Game Text: Nazgul are twilight cost -1.
		* 	Assignment: Make the Free Peoples player assign a companion with 5 or more strength to skirmish this minion.
		*/

		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl harry = scn.GetFreepsCard("harry");

		assertTrue(harry.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, harry.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, harry.getBlueprint().getCulture());
		assertEquals(CardType.MINION, harry.getBlueprint().getCardType());
		assertEquals(Race.MAN, harry.getBlueprint().getRace());
		//assertTrue(scn.HasKeyword(harry, Keyword.SUPPORT_AREA)); // test for keywords as needed
		assertEquals(1, harry.getBlueprint().getTwilightCost());
		assertEquals(3, harry.getBlueprint().getStrength());
		assertEquals(1, harry.getBlueprint().getVitality());
		//assertEquals(, harry.getBlueprint().getResistance());
		//assertEquals(Signet., harry.getBlueprint().getSignet());
		assertEquals(2, harry.getBlueprint().getSiteNumber()); // Change this to getAllyHomeSiteNumbers for allies

	}

	@Test
	public void AllNazgulAreCostMinusOne() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl harry = scn.GetShadowCard("harry");
		PhysicalCardImpl rider = scn.GetShadowCard("rider");
		PhysicalCardImpl wk = scn.GetShadowCard("wk");
		scn.MoveMinionsToTable(harry);
		scn.MoveCardsToHand(rider, wk);

		scn.StartGame();

		scn.SetTwilight(17);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(5, rider.getBlueprint().getTwilightCost());
		assertEquals(8, wk.getBlueprint().getTwilightCost());
		assertEquals(20, scn.GetTwilight());

		scn.ShadowPlayCard(rider);
		assertEquals(14, scn.GetTwilight()); // -5 minion cost, +1 from Harry, -2 roaming

		scn.ShadowPlayCard(wk);
		assertEquals(5, scn.GetTwilight()); // -8 minion cost, +1 from Harry, -2 roaming
	}

	@Test
	public void AssignmentAbilityAssignsHarryToStrength5PlusComp() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl harry = scn.GetShadowCard("harry");
		scn.MoveMinionsToTable(harry);

		PhysicalCardImpl aragorn = scn.GetFreepsCard("aragorn");
		PhysicalCardImpl farin = scn.GetFreepsCard("farin");
		PhysicalCardImpl pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionToTable(aragorn, farin, pippin);

		scn.StartGame();

		scn.SkipToPhase(Phase.ASSIGNMENT);

		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.ShadowActionAvailable(harry));

		scn.ShadowUseCardAction(harry);
		assertTrue(scn.FreepsDecisionAvailable("assign"));
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		scn.FreepsChooseCard(aragorn);
		assertTrue(scn.IsCharAssigned(aragorn));
	}
}
