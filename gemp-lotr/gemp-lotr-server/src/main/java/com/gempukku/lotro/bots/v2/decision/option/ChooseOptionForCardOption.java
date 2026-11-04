package com.gempukku.lotro.bots.v2.decision.option;

import com.gempukku.lotro.game.PhysicalCard;

import java.util.Objects;

public class ChooseOptionForCardOption extends Option {
    private final String choice;
    private final PhysicalCard source;
    private final int sourceId;

    public ChooseOptionForCardOption(PhysicalCard source, String option, int index) {
        super("Choose '" + option + "' for " + source.getBlueprint().getFullName(), String.valueOf(index));
        this.choice = option;
        this.source = source;
        this.sourceId = source.getCardId();
    }

    public String getChoice() {
        return choice;
    }

    public PhysicalCard getSource() {
        return source;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ChooseOptionForCardOption that = (ChooseOptionForCardOption) o;
        return Objects.equals(sourceId, that.sourceId)
                && Objects.equals(source.getBlueprint().getFullName(), that.source.getBlueprint().getFullName())
                && Objects.equals(choice, that.choice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceId, source.getBlueprint().getFullName(), choice);
    }
}
