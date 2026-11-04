package com.gempukku.lotro.bots.v2.decision.option;

import com.gempukku.lotro.bots.BotService;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.PhysicalCard;

import java.util.Objects;

public class ChooseArbitraryTargetForCardOption extends Option {
    private final LotroCardBlueprint blueprint;
    private final PhysicalCard source;
    private final int sourceId;

    public ChooseArbitraryTargetForCardOption(PhysicalCard source, String blueprintId, String tempCardId) throws CardNotFoundException {
        super(getHumanReadableOption(source, blueprintId), tempCardId);
        blueprint = BotService.staticLibrary.getLotroCardBlueprint(blueprintId);
        this.source = source;
        this.sourceId = source.getCardId();
    }

    private static String getHumanReadableOption(PhysicalCard source, String blueprintId) throws CardNotFoundException {
        LotroCardBlueprint blueprint = BotService.staticLibrary.getLotroCardBlueprint(blueprintId);
        return "Choose " + blueprint.getFullName() + " for " + source.getBlueprint().getFullName();
    }

    public LotroCardBlueprint getBlueprint() {
        return blueprint;
    }

    public PhysicalCard getSource() {
        return source;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ChooseArbitraryTargetForCardOption that = (ChooseArbitraryTargetForCardOption) o;
        return Objects.equals(sourceId, that.sourceId)
                && Objects.equals(source.getBlueprint().getFullName(), that.source.getBlueprint().getFullName())
                && Objects.equals(blueprint.getFullName(), that.blueprint.getFullName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceId, blueprint.getFullName(), source.getBlueprint().getFullName());
    }
}
