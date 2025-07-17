package com.gempukku.lotro.bots.rl.v2.decisions.choice;

import java.util.List;

public class AnotherMoveAnswerer extends AbstractChoiceAnswerer {
    @Override
    protected String getTextTrigger() {
        return "another move";
    }

    @Override
    protected List<String> getOptions() {
        return List.of("Yes", "No");
    }
}
