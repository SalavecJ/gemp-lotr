package com.gempukku.lotro.bots.forge.plan.action2;

import com.gempukku.lotro.game.PhysicalCard;

public class ChooseSkirmishAction2 extends ActionToTake2 {
    private final PhysicalCard fpCharacter;

    public ChooseSkirmishAction2(String decisionText, PhysicalCard fpCharacter) {
        super(decisionText);
        this.fpCharacter = fpCharacter;
    }

    public PhysicalCard getFpCharacter() {
        return fpCharacter;
    }

    @Override
    public String carryOut() {
        return String.valueOf(fpCharacter.getCardId());
    }

    @Override
    public String toString() {
        return "Action: Choose " + fpCharacter.getBlueprint().getFullName() + " as next skirmish to resolve";
    }
}
