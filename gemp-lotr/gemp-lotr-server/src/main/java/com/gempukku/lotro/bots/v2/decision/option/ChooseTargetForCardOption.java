package com.gempukku.lotro.bots.v2.decision.option;

import com.gempukku.lotro.game.PhysicalCard;

public class ChooseTargetForCardOption extends Option {
    private final PhysicalCard source;
    private final PhysicalCard target;

    public ChooseTargetForCardOption(PhysicalCard source, PhysicalCard target) {
        super(getHumanReadableOption(source, target), String.valueOf(target.getCardId()));
        this.source = source;
        this.target = target;
    }

    private static String getHumanReadableOption(PhysicalCard source, PhysicalCard target) {
        return "Choose " + target.getBlueprint().getFullName() + " for " + source.getBlueprint().getFullName();
    }

    public PhysicalCard getSource() {
        return source;
    }

    public PhysicalCard getTarget() {
        return target;
    }
}
