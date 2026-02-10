package com.gempukku.lotro.bots.forge.cards.abstractcards;

import com.gempukku.lotro.bots.forge.cards.ability.Ability;
import com.gempukku.lotro.bots.forge.cards.ability.AbilityStep;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.List;

public abstract class BotCard {
    private final PhysicalCard physicalCard;

    public BotCard(PhysicalCard physicalCard) {
        this.physicalCard = physicalCard;
    }

    public final PhysicalCard getPhysicalCard() {
        return physicalCard;
    }

    public final String getFullName() {
        return physicalCard.getBlueprint().getFullName();
    }

    public boolean discardFromHandIfPossible(DefaultLotroGame game) {
        return false;
    }

    public List<Ability> getAbilities() {
        return List.of();
    }

    public final AbilityStep getAbilityStepForDecision(String decisionText) {
        List<AbilityStep> matchingSteps = getAbilities().stream()
                .flatMap(ability -> ability.getSteps().stream())
                .filter(abilityStep -> abilityStep.decisionTextMatches(decisionText))
                .toList();
        if (matchingSteps.isEmpty()) {
            return null;
        } else if (matchingSteps.size() == 1) {
            return matchingSteps.getFirst();
        } else {
            throw new IllegalStateException("Multiple ability steps found for decision text '" + decisionText + "' on card " + this);
        }
    }

    @Override
    public final String toString() {
        return getFullName();
    }
}
