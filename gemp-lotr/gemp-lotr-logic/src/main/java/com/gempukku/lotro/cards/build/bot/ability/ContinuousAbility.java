package com.gempukku.lotro.cards.build.bot.ability;

import com.gempukku.lotro.cards.build.bot.BotTargetingMode;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

import java.util.*;

public class ContinuousAbility implements BotAbility {

    private final AbilityProperty effect;
    private final List<AbilityProperty> activeWhenList;

    public ContinuousAbility(AbilityProperty effect, List<AbilityProperty> activeWhenList) {
        this.effect = effect;
        this.activeWhenList = activeWhenList;
    }

    @Override
    public final AbilityType getType() {
        return AbilityType.CONTINUOUS;
    }

    @Override
    public AbilityProperty getEffect() {
        return effect;
    }

    @Override
    public boolean canProduceDecision(LotroGame game, AwaitingDecision awaitingDecision) {
        return false;
    }

    @Override
    public BotTargetingMode getTargetingModeForDecision(LotroGame game, AwaitingDecision awaitingDecision) {
        return null;
    }

    public List<AbilityProperty> getActiveWhenList() {
        return Collections.unmodifiableList(activeWhenList);
    }
}
