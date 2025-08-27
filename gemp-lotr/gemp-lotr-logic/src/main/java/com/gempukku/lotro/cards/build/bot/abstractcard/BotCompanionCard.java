package com.gempukku.lotro.cards.build.bot.abstractcard;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

public abstract class BotCompanionCard extends BotCharacterCard {
    public BotCompanionCard(PhysicalCard self) {
        super(self);
    }

    @Override
    public boolean canBePlayed(LotroGame game) {
        return super.canBePlayed(game) && ruleOfNineOk(game);
    }

    @Override
    public boolean canEverBePlayed(LotroGame game) {
        return super.canEverBePlayed(game) && ruleOfNineOk(game);
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
