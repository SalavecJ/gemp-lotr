package com.gempukku.lotro.bots.forge.cards.abstractcards;

import com.gempukku.lotro.bots.forge.cards.ability.Ability;
import com.gempukku.lotro.bots.forge.cards.ability.AbilityStep;
import com.gempukku.lotro.bots.forge.cards.ability.AbilityType;
import com.gempukku.lotro.bots.forge.cards.ability.ActivatedAbility;
import com.gempukku.lotro.bots.forge.cards.ability.step.DiscardCardsFromHand;
import com.gempukku.lotro.bots.forge.cards.ability.step.*;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.List;
import java.util.Objects;

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

    public ActivatedAbility getActivatedAbility(Timeword timeword) {
        List<ActivatedAbility> activatedAbilities = getAbilities().stream()
                .filter(ability -> ability instanceof ActivatedAbility activatedAbility && activatedAbility.getTimeword() == timeword)
                .map(ability -> (ActivatedAbility) ability)
                .toList();
        if (activatedAbilities.isEmpty()) {
            return null;
        } else if (activatedAbilities.size() == 1) {
            return activatedAbilities.getFirst();
        } else {
            throw new IllegalStateException("Multiple activated abilities found for timeword " + timeword + " on card " + getFullName());
        }
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

    public final boolean canPlayCardFromHand(Timeword timeword, BotCard botCard) {
        return getAbilities().stream().anyMatch(ability -> ability instanceof ActivatedAbility activatedAbility
                && activatedAbility.getTimeword() == timeword
                && ability.getSteps().stream().anyMatch(abilityStep -> abilityStep instanceof PlayCard playCardEffect && playCardEffect.canPlayCard(botCard)));
    }

    public final boolean canUnclogHand(Timeword timeword) {
        return canPerformAbilityStep(timeword, PutCardsFromHandOnBottomOfDeck.class) || canPerformAbilityStep(timeword, DiscardCardsFromHand.class);
    }

    public final boolean canDiscardCardsFromPlay(Timeword timeword) {
        return canPerformAbilityStep(timeword, DiscardAll.class);
    }

    public final boolean canPlayNextSite(Timeword timeword) {
        return canPerformAbilityStep(timeword, PlayNextSite.class);
    }

    public final boolean canHeal(Timeword timeword) {
        return canPerformAbilityStep(timeword, Heal.class) || canPerformAbilityStep(timeword, HealSelf.class);
    }

    public final boolean canRemoveBurdens(Timeword timeword) {
        return canPerformAbilityStep(timeword, RemoveBurden.class);
    }

    public final boolean canPutCardsFromDiscardIntoHand(Timeword timeword) {
        return canPerformAbilityStep(timeword, PutCardsFromDiscardIntoHand.class);
    }

    public final boolean canRevealOpponentsHand(Timeword timeword) {
        return canPerformAbilityStep(timeword, RevealOpponentsHand.class);
    }

    private boolean hasEventAbilityWithStep(Timeword timeword, Class<?> stepClass) {
        if (this instanceof BotEventCard eventCard) {
            return eventCard.getTimewords().contains(timeword)
                    && eventCard.getAbilities().stream().anyMatch(ability ->
                    ability.getAbilityType() == AbilityType.EVENT
                            && ability.getSteps().stream().anyMatch(stepClass::isInstance));
        }
        return false;
    }

    private boolean hasActivatedAbilityWithStep(Timeword timeword, Class<?> stepClass) {
        return getAbilities().stream().anyMatch(ability -> ability instanceof ActivatedAbility activatedAbility
                && activatedAbility.getTimeword() == timeword
                && ability.getSteps().stream().anyMatch(stepClass::isInstance));
    }

    private boolean canPerformAbilityStep(Timeword timeword, Class<?> stepClass) {
        return hasEventAbilityWithStep(timeword, stepClass) || hasActivatedAbilityWithStep(timeword, stepClass);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BotCard botCard = (BotCard) o;
        return Objects.equals(physicalCard, botCard.physicalCard);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(physicalCard.getCardId());
    }
}
