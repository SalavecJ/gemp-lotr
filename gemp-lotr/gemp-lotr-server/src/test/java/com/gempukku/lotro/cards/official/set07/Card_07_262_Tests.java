package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_262_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("battlements", "7_262");
					put("pillager", "7_275");
					put("assassin", "1_262");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AbovetheBattlementStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Above the Battlement
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: Play a besieger stacked on a site you control or
		 * remove a burden to play a [sauron] Orc from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("battlements");

		assertEquals("Above the Battlement", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AbovetheBattlementCanRemoveABurdenToPlaySauronOrcFromDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var site1 = scn.GetFreepsSite(1);

		var pillager = scn.GetShadowCard("pillager");
		var assassin = scn.GetShadowCard("assassin");
		var battlements = scn.GetShadowCard("battlements");
		scn.MoveCardsToDiscard(assassin, battlements);

		scn.StartGame();

		scn.SkipToSite(2);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowTakeControlOfSite();
		assertTrue(scn.IsSiteControlled(site1));
		scn.StackCardsOn(site1, pillager);
		scn.MoveCardsToHand(battlements);

		scn.SkipToSite(4);
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.IsSiteControlled(site1));
		assertTrue(scn.ShadowControls(site1));
		assertEquals(Zone.STACKED, pillager.getZone());
		assertEquals(Zone.DISCARD, assassin.getZone());
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.ShadowPlayAvailable(battlements));

		scn.ShadowPlayCard(battlements);
		assertEquals(2, scn.ShadowGetChoiceCount());

		scn.ShadowChooseOption("burden");
		assertEquals(Zone.STACKED, pillager.getZone());
		assertEquals(Zone.SHADOW_CHARACTERS, assassin.getZone());
		assertEquals(0, scn.GetBurdens());
	}

	@Test
	public void AbovetheBattlementCanRemoveABurdenToPlaySauronOrcFromDiscardWhenNoStackedOption() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var site1 = scn.GetFreepsSite(1);

		var pillager = scn.GetShadowCard("pillager");
		var assassin = scn.GetShadowCard("assassin");
		var battlements = scn.GetShadowCard("battlements");
		scn.MoveCardsToDiscard(assassin);
		scn.MoveCardsToHand(pillager, battlements);

		scn.StartGame();

		scn.SkipToSite(2);
		scn.FreepsPassCurrentPhaseAction();

		scn.SkipToSite(4);
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Zone.HAND, pillager.getZone());
		assertEquals(Zone.DISCARD, assassin.getZone());
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.ShadowPlayAvailable(battlements));

		scn.ShadowPlayCard(battlements);

		assertEquals(Zone.HAND, pillager.getZone());
		assertEquals(Zone.SHADOW_CHARACTERS, assassin.getZone());
		assertEquals(0, scn.GetBurdens());
	}

	@Test
	public void AbovetheBattlementCanPlayStackedBesieger() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var site1 = scn.GetFreepsSite(1);

		var pillager = scn.GetShadowCard("pillager");
		var assassin = scn.GetShadowCard("assassin");
		var battlements = scn.GetShadowCard("battlements");
		scn.MoveCardsToDiscard(assassin, battlements);

		scn.StartGame();

		scn.SkipToSite(2);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowTakeControlOfSite();
		assertTrue(scn.IsSiteControlled(site1));
		scn.StackCardsOn(site1, pillager);
		scn.MoveCardsToHand(battlements);

		scn.SkipToSite(4);
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.IsSiteControlled(site1));
		assertTrue(scn.ShadowControls(site1));
		assertEquals(Zone.STACKED, pillager.getZone());
		assertEquals(Zone.DISCARD, assassin.getZone());
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.ShadowPlayAvailable(battlements));

		scn.ShadowPlayCard(battlements);
		assertEquals(2, scn.ShadowGetChoiceCount());

		scn.ShadowChooseOption("play");
		assertEquals(Zone.SHADOW_CHARACTERS, pillager.getZone());
		assertEquals(Zone.DISCARD, assassin.getZone());
		assertEquals(1, scn.GetBurdens());
	}
}
