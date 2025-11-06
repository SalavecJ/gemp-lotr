package com.gempukku.lotro.bots.forge.plan.action;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

/**
 * Action representing FP choosing which skirmish to resolve next.
 * During the skirmish phase, FP chooses the order in which skirmishes are resolved.
 * This action is used during planning/minimax exploration to choose which FP character's
 * skirmish to resolve.
 */
public class ChooseSkirmishAction implements ActionToTake {
    private final PhysicalCard fpCharacter;

    public ChooseSkirmishAction(PhysicalCard fpCharacter) {
        this.fpCharacter = fpCharacter;
    }

    public PhysicalCard getFpCharacter() {
        return fpCharacter;
    }

    @Override
    public int carryOut(AwaitingDecision awaitingDecision) {
        // Return the physical card ID of the FP character whose skirmish to resolve
        return fpCharacter.getCardId();
    }

    @Override
    public String toString() {
        return "Action: Choose skirmish for " + fpCharacter.getBlueprint().getFullName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChooseSkirmishAction that = (ChooseSkirmishAction) o;
        return fpCharacter.getCardId() == that.fpCharacter.getCardId();
    }

    @Override
    public int hashCode() {
        return fpCharacter.getCardId();
    }
}

