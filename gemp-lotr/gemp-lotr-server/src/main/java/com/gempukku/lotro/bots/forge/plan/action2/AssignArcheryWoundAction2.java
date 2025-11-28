package com.gempukku.lotro.bots.forge.plan.action2;

import com.gempukku.lotro.game.PhysicalCard;

public class AssignArcheryWoundAction2 extends ActionToTake2 {
    private final PhysicalCard fpCharacter;

    public AssignArcheryWoundAction2(String decisionText, PhysicalCard fpCharacter) {
        super(decisionText);
        this.fpCharacter = fpCharacter;
    }

    @Override
    public String carryOut() {
        return String.valueOf(fpCharacter.getCardId());
    }

    @Override
    public String toString() {
        return "Action: Choose " + fpCharacter.getBlueprint().getFullName() + " to be wounded by archery fire";
    }
}
