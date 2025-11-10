package com.gempukku.lotro.bots.forge.cards.abstractcard;

import com.gempukku.lotro.bots.forge.cards.BotTargetingMode;
import com.gempukku.lotro.bots.forge.cards.ability.BotAbility;
import com.gempukku.lotro.bots.forge.cards.ability2.EventAbility;
import com.gempukku.lotro.bots.forge.cards.ability2.TriggeredAbility;
import com.gempukku.lotro.bots.forge.cards.ability2.effect.Effect;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.bots.forge.plan.PlannedBoardState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

import java.util.ArrayList;
import java.util.List;

public abstract class BotCard {
    protected final PhysicalCard self;

    public BotCard(PhysicalCard self) {
        this.self = self;
    }

    public PhysicalCard getSelf() {
        return self;
    }

    public boolean canBePlayed(PlannedBoardState plannedBoardState) {
        return isPlayableInPhase(plannedBoardState.getCurrentPhase())
                && canBePlayedNoMatterThePhase(plannedBoardState);
    }

    public abstract boolean canBePlayedNoMatterThePhase(PlannedBoardState plannedBoardState);

    protected final boolean uniqueRequirementOk(PlannedBoardState plannedBoardState) {
        return !self.getBlueprint().isUnique() || !plannedBoardState.sameTitleInPlayOrInDeadPile(self.getBlueprint().getTitle(), self.getOwner());
    }

    public abstract List<BotAbility> getAbilities();

    public EventAbility getEventAbility() {
        return null;
    }

    public final com.gempukku.lotro.bots.forge.cards.ability2.ActivatedAbility getActivatedAbility(Class<? extends  Effect> effectClass) {
        return getActivatedAbilities().stream()
                .filter(activatedAbility -> effectClass.isAssignableFrom(activatedAbility.getEffect().getClass()))
                .findFirst().orElse(null);
    }

    public List<com.gempukku.lotro.bots.forge.cards.ability2.ActivatedAbility> getActivatedAbilities() {
        return new ArrayList<>();
    }

    public TriggeredAbility getTriggeredAbility() {
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