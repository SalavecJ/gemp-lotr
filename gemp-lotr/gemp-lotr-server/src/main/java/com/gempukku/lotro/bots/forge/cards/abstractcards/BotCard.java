package com.gempukku.lotro.bots.forge.cards.abstractcards;

import com.gempukku.lotro.bots.forge.cards.ability.Ability;
import com.gempukku.lotro.bots.forge.cards.ability.AbilityStep;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.List;

public abstract class BotCard {
    private PhysicalCard physicalCard;

    public BotCard(PhysicalCard physicalCard) {
        this.physicalCard = physicalCard;
    }

    public PhysicalCard getPhysicalCard() {
        return physicalCard;
    }

    public String getFullName() {
        return physicalCard.getBlueprint().getFullName();
    }

    public boolean discardFromHandIfPossible(DefaultLotroGame game) {
        return false;
    }

    public List<Ability> getAbilities() {
        return List.of();
    }

    public AbilityStep getAbilityStepForDecision(String decisionText) {
        return null;
    }

    @Override
    public String toString() {
        return getFullName();
    }
}
