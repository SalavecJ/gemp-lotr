package com.gempukku.lotro.bots.v2.decision.option;

import com.gempukku.lotro.game.PhysicalCard;

public class ChooseNumberForCardOption extends Option {
    private final int choice;
    private final PhysicalCard source;

    public ChooseNumberForCardOption(PhysicalCard source, int choice) {
        super("Choose " + choice + " for " + source.getBlueprint().getFullName(), String.valueOf(choice));
        this.choice = choice;
        this.source = source;
    }

    public int getChoice() {
        return choice;
    }

    public PhysicalCard getSource() {
        return source;
    }
}
