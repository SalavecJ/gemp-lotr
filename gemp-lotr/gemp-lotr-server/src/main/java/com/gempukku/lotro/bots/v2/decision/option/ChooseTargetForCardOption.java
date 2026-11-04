package com.gempukku.lotro.bots.v2.decision.option;

import com.gempukku.lotro.game.PhysicalCard;

import java.util.Objects;

public class ChooseTargetForCardOption extends Option {
    private final PhysicalCard source;
    private final PhysicalCard target;
    private final int sourceId;
    private final int targetId;

    public ChooseTargetForCardOption(PhysicalCard source, PhysicalCard target) {
        super(getHumanReadableOption(source, target), String.valueOf(target.getCardId()));
        this.source = source;
        this.target = target;
        this.sourceId = source.getCardId();
        this.targetId = target.getCardId();
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ChooseTargetForCardOption that = (ChooseTargetForCardOption) o;
        return Objects.equals(sourceId, that.sourceId) && Objects.equals(targetId, that.targetId)
                && Objects.equals(source.getBlueprint().getFullName(), that.source.getBlueprint().getFullName())
                && Objects.equals(target.getBlueprint().getFullName(), that.target.getBlueprint().getFullName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceId, targetId, source.getBlueprint().getFullName(), target.getBlueprint().getFullName());
    }
}
