package com.gempukku.lotro.bots.forge.cards.abstractcard;

import com.gempukku.lotro.bots.forge.cards.ability2.ActivatedAbility;
import com.gempukku.lotro.bots.forge.cards.ability2.EventAbility;
import com.gempukku.lotro.bots.forge.cards.ability2.TriggeredAbility;
import com.gempukku.lotro.bots.forge.cards.ability2.effect.Effect;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

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

    public boolean canBePlayed(DefaultLotroGame game) {
        return isPlayableInPhase(game.getGameState().getCurrentPhase())
                && canBePlayedNoMatterThePhase(game);
    }

    public abstract boolean canBePlayedNoMatterThePhase(DefaultLotroGame game);

    protected final boolean uniqueRequirementOk(DefaultLotroGame game) {
        return !self.getBlueprint().isUnique() || !game.getGameState().sameTitleInPlayOrInDeadPile(self.getBlueprint().getTitle(), self.getOwner());
    }


    public EventAbility getEventAbility() {
        return null;
    }

    public final ActivatedAbility getActivatedAbility(Class<? extends  Effect> effectClass) {
        return getActivatedAbilities().stream()
                .filter(activatedAbility -> effectClass.isAssignableFrom(activatedAbility.getEffect().getClass()))
                .findFirst().orElse(null);
    }

    public List<ActivatedAbility> getActivatedAbilities() {
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
}