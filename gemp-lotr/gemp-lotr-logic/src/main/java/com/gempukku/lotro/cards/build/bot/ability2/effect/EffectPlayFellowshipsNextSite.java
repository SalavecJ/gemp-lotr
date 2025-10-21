package com.gempukku.lotro.cards.build.bot.ability2.effect;

import com.gempukku.lotro.cards.build.bot.abstractcard.BotCard;
import com.gempukku.lotro.game.state.PlannedBoardState;

public class EffectPlayFellowshipsNextSite extends Effect {

    public EffectPlayFellowshipsNextSite() {

    }

    public BotCard getNextSiteInPlay(PlannedBoardState plannedBoardState) {
        int current = plannedBoardState.getCurrentPlayerPosition();
        int next = current + 1;
        return plannedBoardState.getSitesInPlay().stream().filter(botCard -> botCard.getSelf().getBlueprint().getSiteNumber() == next).findFirst().orElse(null);
    }

    public BotCard getNextSiteInAdventureDeck(String player, PlannedBoardState plannedBoardState) {
        int current = plannedBoardState.getCurrentPlayerPosition();
        int next = current + 1;
        return plannedBoardState.getAdventureDeck(player).stream().filter(botCard -> botCard.getSelf().getBlueprint().getSiteNumber() == next).findFirst().orElse(null);
    }

    @Override
    public void resolve(String player, PlannedBoardState plannedBoardState) {
        plannedBoardState.playFellowshipsNextSite(player);
    }

    @Override
    public double getValueIfResolved(String player, PlannedBoardState plannedBoardState) {
        int current = plannedBoardState.getCurrentPlayerPosition();
        int next = current + 1;

        if (next > 9) {
            // no next site
            return 0.0;
        }

        BotCard nextSite = plannedBoardState.getSitesInPlay().stream().filter(botCard -> botCard.getSelf().getBlueprint().getSiteNumber() == next).findFirst().orElse(null);
        BotCard nextSiteInAdventureDeck = plannedBoardState.getAdventureDeck(player).stream().filter(botCard -> botCard.getSelf().getBlueprint().getSiteNumber() == next).findFirst().orElse(null);

        if (nextSite != null && nextSite.getSelf().getOwner().equals(player)) {
            // next site already mine
            return 0.0;
        } else if (nextSite != null && nextSiteInAdventureDeck != null && nextSite.getSelf().getBlueprint().getTitle().equals(nextSiteInAdventureDeck.getSelf().getBlueprint().getTitle())) {
            // next site in play same as mine
            return 0.0;
        } else if (nextSiteInAdventureDeck != null && nextSiteInAdventureDeck.getSelf().getBlueprint().getTitle().equals("The Bridge of Khazad-dûm")) {
            // next site is The Bridge of Khazad-dûm, opponent will have the same
            return 0.0;
        } else {
            // next site not in play yet, or it's opponent's site
            return 2.0;
        }
    }

    @Override
    public String toString(String player, PlannedBoardState plannedBoardState) {
        BotCard nextSiteInPlay = getNextSiteInPlay(plannedBoardState);
        BotCard nextSiteInAdventureDeck = getNextSiteInAdventureDeck(player, plannedBoardState);
        if (plannedBoardState.getCurrentPlayerPosition() == 9) {
            return "play fellowship's next site, but will have no effect";
        } else if (nextSiteInPlay == null) {
            return "play fellowship's next site: " + nextSiteInAdventureDeck.getSelf().getBlueprint().getFullName();
        } else if (nextSiteInAdventureDeck != null) {
            return "replace fellowship's next site, " + nextSiteInPlay.getSelf().getBlueprint().getFullName() +
                    ", for "+ nextSiteInAdventureDeck.getSelf().getBlueprint().getFullName();
        } else {
            return "play fellowship's next site, but will have no effect";
        }
    }
}
