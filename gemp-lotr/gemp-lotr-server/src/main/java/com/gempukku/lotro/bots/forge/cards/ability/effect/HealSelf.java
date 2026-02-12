package com.gempukku.lotro.bots.forge.cards.ability.effect;

import com.gempukku.lotro.bots.forge.cards.ability.cost.Cost;
import com.gempukku.lotro.bots.forge.cards.ability.targeting.BotTargetingPolicy;
import com.gempukku.lotro.bots.forge.cards.abstractcards.BotCard;
import com.gempukku.lotro.bots.forge.utils.WoundsValueUtil;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

public class HealSelf extends Cost {
    private final BotCard self;
    private final int count;

    public HealSelf(BotCard self, int count) {
        this.self = self;
        this.count = count;
    }

    public HealSelf(BotCard self) {
        this(self, 1);
    }

    @Override
    public BotTargetingPolicy getBotTargetingPolicy() {
        return null;
    }

    @Override
    public String toString() {
        if (count == 1) {
            return "Heal self";
        }
        return "Heal self " + count + " times";
    }

    @Override
    public boolean decisionTextMatches(String decisionText) {
        return false;
    }

    @Override
    public double getValue(DefaultLotroGame game, String playerName) {
        return WoundsValueUtil.evaluateWoundsChangeValue(playerName, game, self.getPhysicalCard(), -count);
    }
}
