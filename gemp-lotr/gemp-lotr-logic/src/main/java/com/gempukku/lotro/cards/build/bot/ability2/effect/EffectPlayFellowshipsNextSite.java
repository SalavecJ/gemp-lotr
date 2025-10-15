package com.gempukku.lotro.cards.build.bot.ability2.effect;

import com.gempukku.lotro.cards.build.bot.abstractcard.BotCard;
import com.gempukku.lotro.game.state.PlannedBoardState;

public class EffectPlayFellowshipsNextSite extends Effect{

    public EffectPlayFellowshipsNextSite() {

    }

    @Override
    public void resolve(BotCard source, PlannedBoardState plannedBoardState) {
        plannedBoardState.playFellowshipsNextSite(source.getSelf().getOwner());
    }

    @Override
    public double getValueIfResolved(BotCard source, PlannedBoardState plannedBoardState) {
        int current = plannedBoardState.getCurrentPlayerPosition();
        int next = current + 1;

        if (next > 9) {
            // no next site
            return 0.0;
        }

        BotCard nextSite = plannedBoardState.getSitesInPlay().stream().filter(botCard -> botCard.getSelf().getBlueprint().getSiteNumber() == next).findFirst().orElse(null);
        BotCard nextSiteInAdventureDeck = plannedBoardState.getAdventureDeck(source.getSelf().getOwner()).stream().filter(botCard -> botCard.getSelf().getBlueprint().getSiteNumber() == next).findFirst().orElse(null);

        if (nextSite != null && nextSite.getSelf().getOwner().equals(source.getSelf().getOwner())) {
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
}
