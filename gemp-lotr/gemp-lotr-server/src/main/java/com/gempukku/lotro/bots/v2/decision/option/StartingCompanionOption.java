package com.gempukku.lotro.bots.v2.decision.option;

import com.gempukku.lotro.bots.BotService;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;

public class StartingCompanionOption extends Option {
    private final LotroCardBlueprint blueprint;

    public StartingCompanionOption(String blueprintId, String tempCardId) throws CardNotFoundException {
        super(getHumanReadableOption(blueprintId), tempCardId);
        blueprint = BotService.staticLibrary.getLotroCardBlueprint(blueprintId);
    }

    private static String getHumanReadableOption(String blueprintId) throws CardNotFoundException {
        LotroCardBlueprint blueprint = BotService.staticLibrary.getLotroCardBlueprint(blueprintId);
        return "Play " + blueprint.getFullName() + " as starting companion";
    }

    public LotroCardBlueprint getBlueprint() {
        return blueprint;
    }
}
