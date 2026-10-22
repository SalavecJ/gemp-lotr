package com.gempukku.lotro.bots.v2.decision.option;

import com.gempukku.lotro.game.PhysicalCard;

public class ChooseOptionForCardOption extends Option {
    private final String choice;
    private final PhysicalCard source;

    public ChooseOptionForCardOption(PhysicalCard source, String option, int index) {
        super("Choose '" + option + "' for " + source.getBlueprint().getFullName(), String.valueOf(index));
        this.choice = option;
        this.source = source;
    }

    public String getChoice() {
        return choice;
    }

    public PhysicalCard getSource() {
        return source;
    }
}
