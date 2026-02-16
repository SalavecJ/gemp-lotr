package com.gempukku.lotro.bots.forge.cards.ability.step;

import com.gempukku.lotro.bots.forge.cards.ability.AbilityStep;
import com.gempukku.lotro.bots.forge.cards.ability.targeting.BotTargetingPolicy;
import com.gempukku.lotro.bots.forge.cards.abstractcards.BotCard;
import com.gempukku.lotro.bots.forge.utils.WoundsValueUtil;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

public class ExertSelf extends AbilityStep {
    private final BotCard self;
    private final int count;

    public ExertSelf(BotCard self, int count) {
        this.self = self;
        this.count = count;
    }

    public ExertSelf(BotCard self) {
        this(self, 1);
    }

    @Override
    public BotTargetingPolicy getBotTargetingPolicy() {
        return null;
    }

    @Override
    public String toString() {
        if (count == 1) {
            return "Exert self";
        }
        return "Exert self " + count + " times";
    }

    @Override
    public boolean decisionTextMatches(String decisionText) {
        return false;
    }

    @Override
    public double getValue(DefaultLotroGame game, String playerName) {
        int realAmount = Math.min(count, game.getModifiersQuerying().getVitality(game, self.getPhysicalCard()) - 1);
        return WoundsValueUtil.evaluateWoundsChangeValue(playerName, game, self.getPhysicalCard(), realAmount);
    }
}
