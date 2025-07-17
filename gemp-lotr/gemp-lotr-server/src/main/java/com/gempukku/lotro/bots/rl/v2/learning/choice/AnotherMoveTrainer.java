package com.gempukku.lotro.bots.rl.v2.learning.choice;

import java.util.List;

public class AnotherMoveTrainer extends AbstractChoiceTrainer {
    @Override
    protected String getTextTrigger() {
        return "another move";
    }

    @Override
    protected List<String> getOptions() {
        return List.of("Yes", "No");
    }
}
