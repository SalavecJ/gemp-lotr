package com.gempukku.lotro.bots.forge.cards.ability.effect;

import com.gempukku.lotro.bots.forge.cards.ability.targeting.BotTargetingPolicy;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.function.Predicate;

public class PlayNextSite extends Effect {

    public PlayNextSite() {

    }

    @Override
    public BotTargetingPolicy getBotTargetingPolicy() {
        return null;
    }

    @Override
    public String toString() {
        return "Play fellowship's next site";
    }

    @Override
    public boolean decisionTextMatches(String decisionText) {
        return false;
    }

    @Override
    public double getValue(DefaultLotroGame game, String playerName) {
        int currentSite = game.getGameState().getPlayerPosition(game.getGameState().getCurrentPlayerId());

        if (currentSite == 9) {
            return 0; // No next site
        }

        PhysicalCard nextSite = game.getGameState().getSite(currentSite + 1);
        if (nextSite == null) {
            return 1; // Next site not played yet
        }

        if (nextSite.getOwner().equals(playerName)) {
            return 0; // Next site already played by the player
        } else {
            PhysicalCard myNextSite = game.getGameState().getAdventureDeck(playerName).stream().filter((Predicate<PhysicalCard>) physicalCard -> physicalCard.getBlueprint().getSiteNumber() == currentSite + 1).findFirst().orElseThrow();
            if (myNextSite.getBlueprint().getFullName().equals(nextSite.getBlueprint().getFullName())) {
                return 0; // Next site already played by the opponent, but it's the same as the one in player's adventure deck
            }
            return 1; // Next site played by opponent, replacement makes sense
        }
    }
}
