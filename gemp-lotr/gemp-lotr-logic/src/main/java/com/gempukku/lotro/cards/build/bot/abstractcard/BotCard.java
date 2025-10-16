package com.gempukku.lotro.cards.build.bot.abstractcard;

import com.gempukku.lotro.cards.build.bot.BotTargetingMode;
import com.gempukku.lotro.cards.build.bot.ability.AbilityProperty;
import com.gempukku.lotro.cards.build.bot.ability.ActivatedAbility;
import com.gempukku.lotro.cards.build.bot.ability.BotAbility;
import com.gempukku.lotro.cards.build.bot.ability2.EventAbility;
import com.gempukku.lotro.cards.build.bot.ability2.effect.Effect;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.state.PlannedBoardState;
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

    public abstract boolean canBePlayed(PlannedBoardState plannedBoardState);

    public abstract List<BotAbility> getAbilities();

    public EventAbility getEventAbility() {
        return null;
    }

    public com.gempukku.lotro.cards.build.bot.ability2.ActivatedAbility getActivatedAbility(Class<? extends  Effect> effectClass) {
        return null;
    }

    public boolean isPlayableInPhase(Phase phase) {
      return switch (phase) {
          case PUT_RING_BEARER, PLAY_STARTING_FELLOWSHIP, BETWEEN_TURNS -> false;
          case FELLOWSHIP -> self.getBlueprint().getSide().equals(Side.FREE_PEOPLE)
                  && (!self.getBlueprint().getCardType().equals(CardType.EVENT)
                  || self.getBlueprint().hasTimeword(Timeword.findByPhase(Phase.FELLOWSHIP)));
          case SHADOW -> self.getBlueprint().getSide().equals(Side.SHADOW)
                  && (!self.getBlueprint().getCardType().equals(CardType.EVENT)
                  || self.getBlueprint().hasTimeword(Timeword.findByPhase(Phase.SHADOW)));
          case MANEUVER, ARCHERY, ASSIGNMENT, SKIRMISH, REGROUP -> self.getBlueprint().getCardType().equals(CardType.EVENT)
                  && self.getBlueprint().hasTimeword(Timeword.findByPhase(phase));
      };
    }

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