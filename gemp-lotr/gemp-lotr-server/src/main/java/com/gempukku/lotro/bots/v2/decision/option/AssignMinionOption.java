package com.gempukku.lotro.bots.v2.decision.option;

import com.gempukku.lotro.game.PhysicalCard;

import java.util.Objects;

public class AssignMinionOption extends Option {

    private final PhysicalCard fpCharacter;
    private final PhysicalCard minion;

    public AssignMinionOption(PhysicalCard fpCharacter, PhysicalCard minion) {
        super(getHumanReadableOption(fpCharacter, minion), fpCharacter != null ? fpCharacter.getCardId() + " " + minion.getCardId() : "");
        this.fpCharacter = fpCharacter;
        this.minion = minion;
    }

    private static String getHumanReadableOption(PhysicalCard fpCharacter, PhysicalCard minion) {
        if (fpCharacter == null) {
            return "Leave " + minion.getBlueprint().getFullName() + " unassigned";
        } else {
            return "Assign " + minion.getBlueprint().getFullName() + " to " + fpCharacter.getBlueprint().getFullName();
        }
    }

    public PhysicalCard getFpCharacter() {
        return fpCharacter;
    }

    public PhysicalCard getMinion() {
        return minion;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        AssignMinionOption that = (AssignMinionOption) o;
        if (fpCharacter == null) {
            return Objects.equals(null, that.fpCharacter) && Objects.equals(minion.getCardId(), that.minion.getCardId());
        }
        return Objects.equals(fpCharacter.getCardId(), that.fpCharacter.getCardId()) && Objects.equals(minion.getCardId(), that.minion.getCardId());
    }

    @Override
    public int hashCode() {
        if (fpCharacter == null) {
            return Objects.hash(null, minion.getCardId());
        }
        return Objects.hash(fpCharacter.getCardId(), minion.getCardId());
    }
}
