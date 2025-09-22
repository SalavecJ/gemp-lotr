package com.gempukku.lotro.cards.build.bot.abstractcard;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

public abstract class BotCompanionCard extends BotCharacterCard {
    public BotCompanionCard(PhysicalCard self) {
        super(self);
    }

    @Override
    public boolean canBePlayed() {
        return super.canBePlayed() && ruleOfNineOk(self.getGame());
    }

    @Override
    public boolean canEverBePlayed() {
        return super.canEverBePlayed() && ruleOfNineOk(self.getGame());
    }

    private boolean ruleOfNineOk(LotroGame game) {
        int companionsInPlay = (int) game.getGameState().getInPlay().stream()
                .filter(pc -> CardType.COMPANION.equals(pc.getBlueprint().getCardType())
                        && pc.getOwner().equals(self.getOwner()))
                .count();

        int companionsInDeadPile = (int) game.getGameState().getDeadPile(self.getOwner()).stream()
                .filter(pc -> CardType.COMPANION.equals(pc.getBlueprint().getCardType()))
                .count();

        return companionsInPlay + companionsInDeadPile < 9;
    }
}
