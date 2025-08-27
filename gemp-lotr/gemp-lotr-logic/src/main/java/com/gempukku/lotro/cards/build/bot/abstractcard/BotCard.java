package com.gempukku.lotro.cards.build.bot.abstractcard;

import com.gempukku.lotro.cards.build.bot.BotTargetingMode;
import com.gempukku.lotro.cards.build.bot.ability.BotAbility;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

import java.util.List;

public abstract class BotCard {
    protected final PhysicalCard self;

    public BotCard(PhysicalCard self) {
        this.self = self;
    }

    public PhysicalCard getSelf() {
        return self;
    }

    public abstract boolean canBePlayed(LotroGame game);

    public abstract boolean canEverBePlayed(LotroGame game);

    public abstract List<BotAbility> getAbilities();

    public BotTargetingMode getTargetingModeForDecision(LotroGame game, AwaitingDecision decision) {
        PhysicalCard fromDecision = game.getGameState().getPhysicalCard(
                Integer.parseInt(decision.getDecisionParameters().get("source")[0])
        );
        if (!fromDecision.equals(self)) {
            throw new IllegalStateException("Wrong bot card implementation selected for: " + decision.toJson().toString()
                    + "; Selected: " + self.getBlueprint().getFullName() + "; Decision card: " + fromDecision.getBlueprint().getFullName());
        }

        for (BotAbility ability : getAbilities()) {
            if (ability.canProduceDecision(game, decision)) {
                return ability.getTargetingModeForDecision(game, decision);
            }
        }
        throw new IllegalStateException("Cannot resolve targeting for decision: " + decision.toJson().toString()
                + "; Selected: " + self.getBlueprint().getFullName());
    }
}