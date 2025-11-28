package com.gempukku.lotro.bots.forge.cards.ability2.effect;


import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

public class EffectPlayFellowshipsNextSite extends Effect {

    public EffectPlayFellowshipsNextSite() {

    }

    public PhysicalCard getNextSiteInPlay(DefaultLotroGame game) {
        int current = game.getGameState().getPlayerPosition(game.getGameState().getCurrentPlayerId());
        int next = current + 1;
        return game.getGameState().getActiveCards().stream().filter(card -> card.getBlueprint().getSiteNumber() == next && CardType.SITE == card.getBlueprint().getCardType()).findFirst().orElse(null);
    }

    public PhysicalCard getNextSiteInAdventureDeck(String player, DefaultLotroGame game) {
        int current = game.getGameState().getPlayerPosition(game.getGameState().getCurrentPlayerId());
        int next = current + 1;
        return game.getGameState().getAdventureDeck(player).stream().filter(card -> card.getBlueprint().getSiteNumber() == next).findFirst().orElse(null);
    }

    @Override
    public double getValueIfResolved(String player, DefaultLotroGame game) {
        int current = game.getGameState().getPlayerPosition(game.getGameState().getCurrentPlayerId());
        int next = current + 1;

        if (next > 9) {
            // no next site
            return 0.0;
        }

        PhysicalCard nextSite = getNextSiteInPlay(game);
        PhysicalCard nextSiteInAdventureDeck = getNextSiteInAdventureDeck(player, game);

        if (nextSite != null && nextSite.getOwner().equals(player)) {
            // next site already mine
            return 0.0;
        } else if (nextSite != null && nextSiteInAdventureDeck != null && nextSite.getBlueprint().getTitle().equals(nextSiteInAdventureDeck.getBlueprint().getTitle())) {
            // next site in play same as mine
            return 0.0;
        } else if (nextSiteInAdventureDeck != null && nextSiteInAdventureDeck.getBlueprint().getTitle().equals("The Bridge of Khazad-dûm")) {
            // next site is The Bridge of Khazad-dûm, opponent will have the same
            return 0.0;
        } else {
            // next site not in play yet, or it's opponent's site
            return 2.0;
        }
    }

    @Override
    public String toString(String player, DefaultLotroGame game) {
        PhysicalCard nextSiteInPlay = getNextSiteInPlay(game);
        PhysicalCard nextSiteInAdventureDeck = getNextSiteInAdventureDeck(player, game);
        if (game.getGameState().getPlayerPosition(game.getGameState().getCurrentPlayerId()) == 9) {
            return "play fellowship's next site, but will have no effect";
        } else if (nextSiteInPlay == null) {
            return "play fellowship's next site: " + nextSiteInAdventureDeck.getBlueprint().getFullName();
        } else if (nextSiteInAdventureDeck != null) {
            return "replace fellowship's next site, " + nextSiteInPlay.getBlueprint().getFullName() +
                    ", for "+ nextSiteInAdventureDeck.getBlueprint().getFullName();
        } else {
            return "play fellowship's next site, but will have no effect";
        }
    }
}
