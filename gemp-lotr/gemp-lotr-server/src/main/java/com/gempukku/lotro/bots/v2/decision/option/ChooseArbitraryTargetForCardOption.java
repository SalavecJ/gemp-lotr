package com.gempukku.lotro.bots.v2.decision.option;

import com.gempukku.lotro.bots.BotService;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.PhysicalCard;

public class ChooseArbitraryTargetForCardOption extends Option {
    private final LotroCardBlueprint blueprint;
    private final PhysicalCard source;

    public ChooseArbitraryTargetForCardOption(PhysicalCard source, String blueprintId, String tempCardId) throws CardNotFoundException {
        super(getHumanReadableOption(source, blueprintId), tempCardId);
        blueprint = BotService.staticLibrary.getLotroCardBlueprint(blueprintId);
        this.source = source;
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
}
